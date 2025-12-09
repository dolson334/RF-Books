package com.rfbooks.backend.nonentities.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkTokenResponse {
    @JsonProperty("link_token")
    private String linkToken;

    public LinkTokenResponse() {}

    public LinkTokenResponse(String linkToken) {
        this.linkToken = linkToken;
    }

    public String getLinkToken() {
        return linkToken;
    }

    public void setLinkToken(String linkToken) {
        this.linkToken = linkToken;
    }
}