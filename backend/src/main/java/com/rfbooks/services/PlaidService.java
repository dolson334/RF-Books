package com.rfbooks.services;

import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.rfbooks.config.AuthContext;
import com.rfbooks.entities.PlaidConnection;
import com.rfbooks.entities.PlaidTransactionEntity;
import com.rfbooks.nonentities.ExchangePublicTokenRequest;
import com.rfbooks.nonentities.LinkTokenResponse;
import com.rfbooks.nonentities.PlaidTransaction;
import com.rfbooks.repos.PlaidConnectionRepository;
import com.rfbooks.repos.PlaidTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaidService {

    private final PlaidApi plaidClient;
    private final String clientId;
    private final String secret;
    private final PlaidConnectionRepository connectionRepository;
    private final PlaidTransactionRepository transactionRepository;

    public PlaidService(@Value("${plaid.client-id}") String clientId,
                        @Value("${plaid.secret}") String secret,
                        @Value("${plaid.environment:sandbox}") String environment,
                        PlaidConnectionRepository connectionRepository,
                        PlaidTransactionRepository transactionRepository) {
        this.clientId = clientId;
        this.secret = secret;
        this.connectionRepository = connectionRepository;
        this.transactionRepository = transactionRepository;

        ApiClient apiClient = new ApiClient();

        switch (environment.toLowerCase()) {
            case "production":
                apiClient.setPlaidAdapter(ApiClient.Production);
                break;
            default:
                apiClient.setPlaidAdapter(ApiClient.Sandbox);
                break;
        }

        this.plaidClient = apiClient.createService(PlaidApi.class);
    }

    public LinkTokenResponse createLinkToken() {
        try {
            String userId = AuthContext.getCurrentUserId();
            LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                    .clientId(clientId)
                    .secret(secret)
                    .user(new LinkTokenCreateRequestUser().clientUserId(userId))
                    .clientName("RF-Books")
                    .products(Arrays.asList(Products.TRANSACTIONS))
                    .countryCodes(Arrays.asList(CountryCode.US))
                    .language("en");

            retrofit2.Response<LinkTokenCreateResponse> response =
                    plaidClient.linkTokenCreate(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                return new LinkTokenResponse(response.body().getLinkToken());
            } else {
                throw new RuntimeException("Failed to create link token: " +
                                           response.errorBody().string());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating link token", e);
        }
    }

    public void exchangePublicToken(ExchangePublicTokenRequest request) {
        try {
            ItemPublicTokenExchangeRequest exchangeRequest =
                    new ItemPublicTokenExchangeRequest()
                            .clientId(clientId)
                            .secret(secret)
                            .publicToken(request.getPublicToken());

            retrofit2.Response<ItemPublicTokenExchangeResponse> response =
                    plaidClient.itemPublicTokenExchange(exchangeRequest).execute();

            if (response.isSuccessful() && response.body() != null) {
                String accessToken = response.body().getAccessToken();
                String itemId = response.body().getItemId();
                String userId = AuthContext.getCurrentUserId();

                PlaidConnection connection = connectionRepository
                        .findByUserIdAndActiveTrue(userId)
                        .orElse(new PlaidConnection(userId, accessToken, itemId));

                connection.setAccessToken(accessToken);
                connection.setItemId(itemId);
                connection.setInstitutionName(request.getInstitutionName());
                connection.setConnectedAt(Instant.now());

                connectionRepository.save(connection);
            } else {
                throw new RuntimeException("Failed to exchange public token: " +
                                           response.errorBody().string());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error exchanging public token", e);
        }
    }

    /**
     * Sync transactions using Plaid Sync API (incremental, cursor-based).
     * Falls back to local DB transactions if no Plaid connection exists.
     */
    public List<PlaidTransaction> syncTransactions() {
        String userId = AuthContext.getCurrentUserId();

        PlaidConnection connection = connectionRepository
                .findByUserIdAndActiveTrue(userId)
                .orElse(null);

        if (connection == null) {
            // Return local transactions if no Plaid connection
            return transactionRepository.findByUserId(userId).stream()
                    .map(this::mapEntityToPlaidTransaction)
                    .collect(Collectors.toList());
        }

        try {
            List<PlaidTransactionEntity> added = new ArrayList<>();
            List<String> removedIds = new ArrayList<>();
            String cursor = connection.getSyncCursor();
            boolean hasMore = true;

            while (hasMore) {
                TransactionsSyncRequest syncRequest = new TransactionsSyncRequest()
                        .clientId(clientId)
                        .secret(secret)
                        .accessToken(connection.getAccessToken());

                if (cursor != null && !cursor.isEmpty()) {
                    syncRequest.setCursor(cursor);
                }

                retrofit2.Response<TransactionsSyncResponse> response =
                        plaidClient.transactionsSync(syncRequest).execute();

                if (response.isSuccessful() && response.body() != null) {
                    TransactionsSyncResponse body = response.body();

                    // Process added transactions
                    for (Transaction tx : body.getAdded()) {
                        PlaidTransactionEntity entity = mapToEntity(tx, userId);
                        added.add(entity);
                    }

                    // Process removed transaction IDs
                    for (RemovedTransaction removed : body.getRemoved()) {
                        removedIds.add(removed.getTransactionId());
                    }

                    cursor = body.getNextCursor();
                    hasMore = body.getHasMore();
                } else {
                    throw new RuntimeException("Plaid sync failed: " +
                                               response.errorBody().string());
                }
            }

            // Persist changes
            if (!added.isEmpty()) {
                transactionRepository.saveAll(added);
            }
            // Remove deleted transactions (soft delete or hard delete)
            // For simplicity, we'll skip removal for now

            // Update cursor
            connection.setSyncCursor(cursor);
            connection.setLastSyncedAt(Instant.now());
            connectionRepository.save(connection);

            // Return all transactions from DB
            return transactionRepository.findByUserId(userId).stream()
                    .map(this::mapEntityToPlaidTransaction)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Error syncing transactions", e);
        }
    }

    /**
     * Get transactions by date range from local DB (does not call Plaid API directly).
     */
    public List<PlaidTransaction> getTransactions(String startDate, String endDate) {
        String userId = AuthContext.getCurrentUserId();

        LocalDate start = LocalDate.parse(startDate.substring(0, 10));
        LocalDate end = LocalDate.parse(endDate.substring(0, 10));

        List<PlaidTransactionEntity> localTransactions =
                transactionRepository.findByUserIdAndDateRange(userId, start, end);

        if (!localTransactions.isEmpty()) {
            return localTransactions.stream()
                    .map(this::mapEntityToPlaidTransaction)
                    .collect(Collectors.toList());
        }

        // If no local data, try Plaid API with legacy transactionsGet
        PlaidConnection connection = connectionRepository
                .findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException(
                        "No active Plaid connection found. Please connect your bank account first."));

        try {
            TransactionsGetRequest request = new TransactionsGetRequest()
                    .clientId(clientId)
                    .secret(secret)
                    .accessToken(connection.getAccessToken())
                    .startDate(start)
                    .endDate(end);

            retrofit2.Response<TransactionsGetResponse> response =
                    plaidClient.transactionsGet(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                connection.setLastSyncedAt(Instant.now());
                connectionRepository.save(connection);

                List<PlaidTransaction> results = response.body().getTransactions().stream()
                        .map(this::mapToPlaidTransaction)
                        .collect(Collectors.toList());

                // Persist to local DB for future queries
                List<PlaidTransactionEntity> entities = response.body().getTransactions().stream()
                        .map(tx -> mapToEntity(tx, userId))
                        .collect(Collectors.toList());
                transactionRepository.saveAll(entities);

                return results;
            } else {
                throw new RuntimeException("Failed to get transactions: " +
                                           response.errorBody().string());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error getting transactions", e);
        }
    }

    /**
     * Get account balances from Plaid.
     */
    public Double getAccountBalance() {
        String userId = AuthContext.getCurrentUserId();
        PlaidConnection connection = connectionRepository
                .findByUserIdAndActiveTrue(userId)
                .orElse(null);

        if (connection == null) return null;

        try {
            AccountsBalanceGetRequest request = new AccountsBalanceGetRequest()
                    .clientId(clientId)
                    .secret(secret)
                    .accessToken(connection.getAccessToken());

            retrofit2.Response<AccountsGetResponse> response =
                    plaidClient.accountsBalanceGet(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body().getAccounts().stream()
                        .filter(a -> a.getBalances().getCurrent() != null)
                        .mapToDouble(a -> a.getBalances().getCurrent())
                        .sum();
            }
        } catch (IOException e) {
            // Non-fatal — return null if balance unavailable
        }
        return null;
    }

    public boolean hasActiveConnection(String userId) {
        return connectionRepository.findByUserIdAndActiveTrue(userId).isPresent();
    }

    public void disconnectBank(String userId) {
        connectionRepository.findByUserIdAndActiveTrue(userId)
                .ifPresent(connection -> {
                    connection.setActive(false);
                    connectionRepository.save(connection);
                });
    }

    private PlaidTransactionEntity mapToEntity(Transaction tx, String userId) {
        PlaidTransactionEntity entity = new PlaidTransactionEntity();
        entity.setUserId(userId);
        entity.setTransactionId(tx.getTransactionId());
        entity.setAccountId(tx.getAccountId());
        entity.setAmount(tx.getAmount());
        entity.setDate(tx.getDate());
        entity.setName(tx.getName());
        entity.setMerchantName(tx.getMerchantName());
        entity.setPending(tx.getPending());
        entity.setCategory(tx.getCategory() != null ? String.join(", ", tx.getCategory()) : null);
        return entity;
    }

    private PlaidTransaction mapEntityToPlaidTransaction(PlaidTransactionEntity entity) {
        PlaidTransaction tx = new PlaidTransaction();
        tx.setTransactionId(entity.getTransactionId());
        tx.setAccountId(entity.getAccountId());
        tx.setAmount(entity.getAmount());
        tx.setDate(entity.getDate().toString());
        tx.setName(entity.getName());
        tx.setMerchantName(entity.getMerchantName());
        tx.setPending(entity.getPending());
        if (entity.getCategory() != null) {
            tx.setCategory(Arrays.asList(entity.getCategory().split(", ")));
        }
        return tx;
    }

    private PlaidTransaction mapToPlaidTransaction(Transaction plaidTx) {
        PlaidTransaction tx = new PlaidTransaction();
        tx.setTransactionId(plaidTx.getTransactionId());
        tx.setAccountId(plaidTx.getAccountId());
        tx.setAmount(plaidTx.getAmount());
        tx.setDate(plaidTx.getDate().toString());
        tx.setName(plaidTx.getName());
        tx.setMerchantName(plaidTx.getMerchantName());
        tx.setCategory(plaidTx.getCategory());
        tx.setPending(plaidTx.getPending());
        return tx;
    }
}

