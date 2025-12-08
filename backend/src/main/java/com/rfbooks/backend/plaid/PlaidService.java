package com.rfbooks.backend.plaid;


import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;



@Service
public class PlaidService {

    private final PlaidApi plaidClient;
    private final String clientId;
    private final String secret;

    public PlaidService(@Value("${plaid.client-id}") String clientId,
                        @Value("${plaid.secret}") String secret,
                        @Value("${plaid.environment:sandbox}") String environment) {
        this.clientId = clientId;
        this.secret = secret;

        // Initialize Plaid client
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
                    .user(new LinkTokenCreateRequestUser().clientUserId("user-id"))
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

                // Store accessToken and itemId in your database
                // saveToDatabase(accessToken, itemId, request.getInstitutionName());

            } else {
                throw new RuntimeException("Failed to exchange public token: " +
                                           response.errorBody().string());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error exchanging public token", e);
        }
    }
}