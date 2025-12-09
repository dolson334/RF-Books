package com.rfbooks.dtos;

public class ManualMatchRequest {
    private String paymentId;
    private String transactionId;

    public ManualMatchRequest() {}

    public ManualMatchRequest(String paymentId, String transactionId) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
