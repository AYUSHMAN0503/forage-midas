package com.jpmc.midascore.foundation;

public class Transaction {

    private String senderId;
    private String recipientId;
    private float amount;

    public Transaction() {
    }

    public Transaction(String senderId, String recipientId, float amount) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.amount = amount;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "senderId='" + senderId + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
