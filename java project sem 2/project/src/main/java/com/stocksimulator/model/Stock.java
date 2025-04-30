package com.stocksimulator.model;

import java.io.Serializable;

public class Stock implements Serializable {
    private String symbol;
    private double price;
    private double change;
    private int quantity;
    private boolean isIntraday;
    private double targetMargin;

    public Stock(String symbol, double price, double change) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.quantity = 0;
        this.isIntraday = false;
        this.targetMargin = 0.0;
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isIntraday() { return isIntraday; }
    public void setIntraday(boolean intraday) { this.isIntraday = intraday; }
    public double getTargetMargin() { return targetMargin; }
    public void setTargetMargin(double targetMargin) { this.targetMargin = targetMargin; }
}