package com.rfbooks.backend.services;

import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.rfbooks.backend.entities.PlaidConnection;
import com.rfbooks.backend.nonentities.plaid.ExchangePublicTokenRequest;
import com.rfbooks.backend.nonentities.plaid.LinkTokenResponse;
import com.rfbooks.backend.nonentities.plaid.PlaidTransaction;
import com.rfbooks.backend.repos.PlaidConnectionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaidService {

    private final PlaidApi plaidClient;
    private final String clientId;
    private final String secret;
    private final PlaidConnectionRepository connectionRepository;

    public PlaidService(@Value("${plaid.client-id}") String clientId,
                        @Value("${plaid.secret}") String secret,
                        @Value("${plaid.environment:sandbox}") String environment,
                        PlaidConnectionRepository connectionRepository) {
        this.clientId = clientId;
        this.secret = secret;
        this.connectionRepository = connectionRepository;

        ApiClient apiClient = new ApiClient();

        switch (environment.toLowerCase()) {
            case "production":
                apiClient.setPlaidAdapter(ApiClient.Production);
                break;
            case "development":
                apiClient.setPlaidAdapter(ApiClient.Development);
                break;
            default:
                apiClient.setPlaidAdapter(ApiClient.Sandbox);
                break;
        }

        this.plaidClient = apiClient.createService(PlaidApi.class);
    }

    public LinkTokenResponse createLinkToken() {
        try {
            LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                    .clientId(clientId)
                    .secret(secret)
                    .user(new LinkTokenCreateRequestUser().clientUserId("default-user"))
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

                // Save to database
                String userId = "default-user"; // TODO: Get from authentication context

                // Check if connection already exists
                PlaidConnection connection = connectionRepository
                        .findByUserIdAndActiveTrue(userId)
                        .orElse(new PlaidConnection(userId, accessToken, itemId));

                // Update with new token
                connection.setAccessToken(accessToken);
                connection.setItemId(itemId);
                connection.setInstitutionName(request.getInstitutionName());
                connection.setConnectedAt(Instant.now());

                connectionRepository.save(connection);

                System.out.println("Saved Plaid connection for user: " + userId);

            } else {
                throw new RuntimeException("Failed to exchange public token: " +
                                           response.errorBody().string());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error exchanging public token", e);
        }
    }

    public List<PlaidTransaction> getTransactions(String startDate, String endDate) {
        // Get access token from database
        String userId = "default-user"; // TODO: Get from authentication context

        PlaidConnection connection = connectionRepository
                .findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException(
                        "No active Plaid connection found. Please connect your bank account first."));

        try {
            // Convert ISO dates to LocalDate format (YYYY-MM-DD)
            LocalDate start = LocalDate.parse(startDate.substring(0, 10));
            LocalDate end = LocalDate.parse(endDate.substring(0, 10));

            TransactionsGetRequest request = new TransactionsGetRequest()
                    .clientId(clientId)
                    .secret(secret)
                    .accessToken(connection.getAccessToken())
                    .startDate(start)
                    .endDate(end);

            retrofit2.Response<TransactionsGetResponse> response =
                    plaidClient.transactionsGet(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                // Update last synced time
                connection.setLastSyncedAt(Instant.now());
                connectionRepository.save(connection);

                return response.body().getTransactions().stream()
                        .map(this::mapToPlaidTransaction)
                        .collect(Collectors.toList());
            } else {
                throw new RuntimeException("Failed to get transactions: " +
                                           response.errorBody().string());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error getting transactions", e);
        }
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

    /**
     * Check if user has an active Plaid connection
     */
    public boolean hasActiveConnection(String userId) {
        return connectionRepository.findByUserIdAndActiveTrue(userId).isPresent();
    }

    /**
     * Disconnect/deactivate a Plaid connection
     */
    public void disconnectBank(String userId) {
        connectionRepository.findByUserIdAndActiveTrue(userId)
                .ifPresent(connection -> {
                    connection.setActive(false);
                    connectionRepository.save(connection);
                });
    }
}