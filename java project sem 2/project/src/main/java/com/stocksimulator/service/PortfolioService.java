package com.stocksimulator.service;

import com.stocksimulator.config.DatabaseConfig;
import com.stocksimulator.model.Stock;
import com.stocksimulator.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortfolioService {
    
    public void saveTransaction(Transaction transaction, String username) throws SQLException {
        String sql = "INSERT INTO transactions (username, symbol, quantity, price, type, timestamp, is_intraday, target_margin) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, transaction.getSymbol());
            pstmt.setInt(3, transaction.getQuantity());
            pstmt.setDouble(4, transaction.getPrice());
            pstmt.setString(5, transaction.getType());
            pstmt.setTimestamp(6, Timestamp.valueOf(transaction.getTimestamp()));
            pstmt.setBoolean(7, transaction.isIntraday());
            pstmt.setDouble(8, transaction.getTargetMargin());
            
            pstmt.executeUpdate();
        }
    }
    
    public List<Transaction> getTransactionHistory(String username) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE username = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = new Transaction(
                    rs.getString("symbol"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getString("type"),
                    rs.getBoolean("is_intraday"),
                    rs.getDouble("target_margin")
                );
                transactions.add(transaction);
            }
        }
        return transactions;
    }
    
    public void updatePortfolio(String username, Stock stock) throws SQLException {
        String sql = "INSERT INTO portfolio (username, symbol, quantity, price, is_intraday, target_margin) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "quantity = quantity + ?, " +
                    "price = ?, " +
                    "is_intraday = ?, " +
                    "target_margin = ?";
                    
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, stock.getSymbol());
            pstmt.setInt(3, stock.getQuantity());
            pstmt.setDouble(4, stock.getPrice());
            pstmt.setBoolean(5, stock.isIntraday());
            pstmt.setDouble(6, stock.getTargetMargin());
            pstmt.setInt(7, stock.getQuantity());
            pstmt.setDouble(8, stock.getPrice());
            pstmt.setBoolean(9, stock.isIntraday());
            pstmt.setDouble(10, stock.getTargetMargin());
            
            pstmt.executeUpdate();
        }
    }
    
    public List<Stock> getPortfolio(String username) throws SQLException {
        List<Stock> portfolio = new ArrayList<>();
        String sql = "SELECT * FROM portfolio WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Stock stock = new Stock(
                    rs.getString("symbol"),
                    rs.getDouble("price"),
                    0.0 // Change will be updated from current market data
                );
                stock.setQuantity(rs.getInt("quantity"));
                stock.setIntraday(rs.getBoolean("is_intraday"));
                stock.setTargetMargin(rs.getDouble("target_margin"));
                portfolio.add(stock);
            }
        }
        return portfolio;
    }
    
    public void removeFromPortfolio(String username, String symbol, int quantity) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // First update
            String updateSql = "UPDATE portfolio SET quantity = quantity - ? WHERE username = ? AND symbol = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, quantity);
                updateStmt.setString(2, username);
                updateStmt.setString(3, symbol);
                updateStmt.executeUpdate();
            }

            // Then delete if needed
            String deleteSql = "DELETE FROM portfolio WHERE username = ? AND symbol = ? AND quantity <= 0";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, username);
                deleteStmt.setString(2, symbol);
                deleteStmt.executeUpdate();
            }
        }
    }
} 