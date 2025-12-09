// ChartOfAccountDTO.java
package com.rfbooks.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rfbooks.entities.ChartOfAccount;
import com.rfbooks.enums.AccountType;

public class ChartOfAccountDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("accountType")
    private AccountType accountType;

    @JsonProperty("description")
    private String description;

    // Constructors
    public ChartOfAccountDTO() {}

    public ChartOfAccountDTO(ChartOfAccount entity) {
        this.id = entity.getId();
        this.accountNumber = entity.getAccountNumber();
        this.accountName = entity.getAccountName();
        this.accountType = entity.getAccountType();
        this.description = entity.getDescription();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Convert DTO to Entity
    public ChartOfAccount toEntity() {
        ChartOfAccount entity = new ChartOfAccount();
        entity.setAccountNumber(this.accountNumber);
        entity.setAccountName(this.accountName);
        entity.setAccountType(this.accountType);
        entity.setDescription(this.description);
        return entity;
    }
}