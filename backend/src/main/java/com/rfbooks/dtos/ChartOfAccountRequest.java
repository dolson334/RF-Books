package com.rfbooks.dtos;

public class ChartOfAccountRequest {
    private String accountNumber;
    private String accountName;
    private String accountType;
    private String description;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
