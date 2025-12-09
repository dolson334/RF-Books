package com.rfbooks.controllers;

import com.rfbooks.nonentities.ExchangePublicTokenRequest;
import com.rfbooks.nonentities.LinkTokenResponse;
import com.rfbooks.nonentities.PlaidTransaction;
import com.rfbooks.nonentities.TransactionsRequest;
import com.rfbooks.services.PlaidService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {

    private final PlaidService plaidService;

    public PlaidController(PlaidService plaidService) {
        this.plaidService = plaidService;
    }

    @PostMapping("/link-token")
    public ResponseEntity<LinkTokenResponse> createLinkToken() {
        LinkTokenResponse response = plaidService.createLinkToken();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exchange")
    public ResponseEntity<Void> exchangePublicToken(
            @RequestBody ExchangePublicTokenRequest request) {
        plaidService.exchangePublicToken(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transactions")
    public ResponseEntity<List<PlaidTransaction>> getTransactions(
            @RequestBody TransactionsRequest request) {
        List<PlaidTransaction> transactions = plaidService.getTransactions(
                request.getStartDate(),
                request.getEndDate()
        );
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status")
    public ResponseEntity<ConnectionStatus> getConnectionStatus() {
        String userId = "default-user"; // TODO: Get from auth
        boolean connected = plaidService.hasActiveConnection(userId);
        return ResponseEntity.ok(new ConnectionStatus(connected));
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<Void> disconnectBank() {
        String userId = "default-user"; // TODO: Get from auth
        plaidService.disconnectBank(userId);
        return ResponseEntity.ok().build();
    }

    // Inner class or separate file
    public static class ConnectionStatus {
        private boolean connected;

        public ConnectionStatus(boolean connected) {
            this.connected = connected;
        }

        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
    }
}