package com.stocksimulator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String username;
    private String password;
    private double balance;
    private List<Stock> portfolio;
    private List<Transaction> transactions;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = 100000.00; // Starting balance
        this.portfolio = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    // Getters and setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public List<Stock> getPortfolio() { return portfolio; }
    public List<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void updatePortfolio(Stock stock, String type) {
        if (type.equals("BUY")) {
            boolean found = false;
            for (Stock s : portfolio) {
                if (s.getSymbol().equals(stock.getSymbol())) {
                    s.setQuantity(s.getQuantity() + stock.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                portfolio.add(new Stock(stock.getSymbol(), stock.getPrice(), stock.getChange()));
                portfolio.get(portfolio.size() - 1).setQuantity(stock.getQuantity());
            }
        } else if (type.equals("SELL")) {
            for (Stock s : portfolio) {
                if (s.getSymbol().equals(stock.getSymbol())) {
                    s.setQuantity(s.getQuantity() - stock.getQuantity());
                    if (s.getQuantity() <= 0) {
                        portfolio.remove(s);
                    }
                    break;
                }
            }
        }
    }
} 