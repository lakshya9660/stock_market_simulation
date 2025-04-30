package com.stocksimulator.model;

import java.time.LocalDateTime;

public class Transaction {
    private String symbol;
    private int quantity;
    private double price;
    private String type; // "BUY" or "SELL"
    private LocalDateTime timestamp;
    private boolean isIntraday;
    private double targetMargin;

    public Transaction(String symbol, int quantity, double price, String type, boolean isIntraday, double targetMargin) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.isIntraday = isIntraday;
        this.targetMargin = targetMargin;
    }

    // Constructor overload for backward compatibility
    public Transaction(String symbol, int quantity, double price, String type) {
        this(symbol, quantity, price, type, false, 0.0);
    }

    // Getters
    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isIntraday() { return isIntraday; }
    public double getTargetMargin() { return targetMargin; }
}