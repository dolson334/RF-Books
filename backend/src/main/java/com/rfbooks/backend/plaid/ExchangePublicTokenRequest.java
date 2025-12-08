package com.rfbooks.backend.plaid;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ExchangePublicTokenRequest {
    @JsonProperty("publicToken")
    private String publicToken;

    @JsonProperty("institutionName")
    private String institutionName;

    public ExchangePublicTokenRequest() {}

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
}