package com.digio.kafka.transactions;

public class Transaction {

    private String customer;
    private int amount;
    private String category;
    private long occurred;

    public String getCustomer() {
        return customer;
    }

    public Transaction setCustomer(String customer) {
        this.customer = customer;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public Transaction setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public long getOccurred() {
        return occurred;
    }

    public Transaction setOccurred(long occurred) {
        this.occurred = occurred;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public Transaction setCategory(String category) {
        this.category = category;
        return this;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "customer='" + customer + '\'' +
                ", amount=" + amount +
                ", category=" + category +
                ", occurred=" + occurred +
                '}';
    }
}
