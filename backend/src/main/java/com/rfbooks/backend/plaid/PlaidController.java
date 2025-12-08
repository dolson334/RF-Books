package com.rfbooks.backend.plaid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
