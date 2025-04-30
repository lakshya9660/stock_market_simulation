-- Create database if not exists
CREATE DATABASE IF NOT EXISTS stock_simulator;
USE stock_simulator;

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    type VARCHAR(10) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    is_intraday BOOLEAN NOT NULL,
    target_margin DOUBLE NOT NULL,
    INDEX idx_username (username),
    INDEX idx_timestamp (timestamp)
);

-- Create portfolio table
CREATE TABLE IF NOT EXISTS portfolio (
    username VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    is_intraday BOOLEAN NOT NULL,
    target_margin DOUBLE NOT NULL,
    PRIMARY KEY (username, symbol),
    INDEX idx_username (username)
);