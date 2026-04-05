package com.rfbooks.dtos;

import java.util.ArrayList;
import java.util.List;

public class IncomeImportResponse {
    private int created;
    private int skipped;
    private int errors;
    private List<String> errorDetails;

    public IncomeImportResponse() {
        this.errorDetails = new ArrayList<>();
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public List<String> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<String> errorDetails) {
        this.errorDetails = errorDetails;
    }

    public void addError(String detail) {
        this.errorDetails.add(detail);
        this.errors++;
    }

    public void incrementCreated() {
        this.created++;
    }

    public void incrementSkipped() {
        this.skipped++;
    }
}
