package com.rfbooks.backend.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PlaidTransaction {
    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("date")
    private String date;

    @JsonProperty("name")
    private String name;

    @JsonProperty("merchantName")
    private String merchantName;

    @JsonProperty("category")
    private List<String> category;

    @JsonProperty("pending")
    private Boolean pending;

    public PlaidTransaction() {}

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public List<String> getCategory() { return category; }
    public void setCategory(List<String> category) { this.category = category; }

    public Boolean getPending() { return pending; }
    public void setPending(Boolean pending) { this.pending = pending; }
}