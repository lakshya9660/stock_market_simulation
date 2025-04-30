package com.stocksimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.SQLException;

import com.stocksimulator.model.Stock;
import com.stocksimulator.model.Transaction;
import com.stocksimulator.model.User;
import com.stocksimulator.service.StockService;
import com.stocksimulator.service.UserService;
import com.stocksimulator.service.PortfolioService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class StockSimulatorApp extends Application {
    private final StockService stockService = new StockService();
    private final UserService userService = new UserService();
    private final PortfolioService portfolioService = new PortfolioService();
    private double balance = 100000.00;
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Stock> portfolio = new ArrayList<>();
    private Stock currentStock;
    private LineChart<Number, Number> chart;
    private Label balanceLabel;
    private VBox stockInfo;
    private VBox loginBox;
    private VBox mainContent;
    private Stage primaryStage;
    private Stage portfolioStage;
    private Stage historyStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Create login screen
        createLoginScreen(root);

        // Create scene and add styles
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        // Add window close event handler
        primaryStage.setOnCloseRequest(event -> {
            if (userService.getCurrentUser() != null) {
                userService.logout(); // This will save the data
            }
        });

        primaryStage.setTitle("Stock Market Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createLoginScreen(BorderPane root) {
        loginBox = new VBox(30);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        loginBox.getStyleClass().add("login-box");

        // Title
        Label titleLabel = new Label("STOCK MARKET SIMULATOR");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #00ff9d;");

        // Subtitle
        Label subtitleLabel = new Label("Future of Trading");
        subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffffff;");

        // Input fields container
        VBox inputBox = new VBox(20);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setMaxWidth(300);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);

        // Buttons container
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginBtn = new Button("LOGIN");
        loginBtn.setPrefWidth(120);
        loginBtn.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        Button registerBtn = new Button("REGISTER");
        registerBtn.setPrefWidth(120);
        registerBtn.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText()));

        buttonBox.getChildren().addAll(loginBtn, registerBtn);

        inputBox.getChildren().addAll(usernameField, passwordField, buttonBox);
        loginBox.getChildren().addAll(titleLabel, subtitleLabel, inputBox);

        root.setCenter(loginBox);
    }

    private void handleLogin(String username, String password) {
        if (userService.login(username, password)) {
            showMainContent();
        } else {
            showError("Invalid username or password");
        }
    }

    private void handleRegister(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty");
            return;
        }
        
        if (userService.register(username, password)) {
            showMainContent();
        } else {
            showError("Username already exists");
        }
    }

    private void showMainContent() {
        // Update window title
        primaryStage.setTitle("Stock Market Simulator - " + userService.getCurrentUser().getUsername());
        
        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
        root.getChildren().clear();

        // Top navigation
        HBox topNav = createTopNav();
        root.setTop(topNav);

        // Center content with chart
        SplitPane centerContent = createCenterContent();
        root.setCenter(centerContent);

        // Start real-time updates
        startRealTimeUpdates();
    }

    private HBox createTopNav() {
        HBox topNav = new HBox(20);
        topNav.getStyleClass().add("top-nav");
        topNav.setPadding(new Insets(15));
        topNav.setAlignment(Pos.CENTER_LEFT);

        balanceLabel = new Label(String.format("Balance: $%.2f", userService.getCurrentUser().getBalance()));
        balanceLabel.getStyleClass().add("balance-label");

        Button portfolioBtn = new Button("Portfolio");
        Button tradeBtn = new Button("Trade");
        Button historyBtn = new Button("History");
        Button logoutBtn = new Button("Logout");

        portfolioBtn.setOnAction(e -> showPortfolio());
        tradeBtn.setOnAction(e -> showTradePanel());
        historyBtn.setOnAction(e -> showHistory());
        logoutBtn.setOnAction(e -> handleLogout());

        topNav.getChildren().addAll(balanceLabel, portfolioBtn, tradeBtn, historyBtn, logoutBtn);
        return topNav;
    }

    private void showTradePanel() {
        // Get the root BorderPane
        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
        
        // Create new center content with trading panel
        SplitPane centerContent = createCenterContent();
        root.setCenter(centerContent);
        
        // Set divider position to show trading panel
        centerContent.setDividerPositions(0.7);
    }

    private SplitPane createCenterContent() {
        SplitPane splitPane = new SplitPane();
        
        // Left side - Chart
        VBox chartBox = createChartSection();
        
        // Right side - Trading panel
        VBox tradingBox = createTradingSection();

        splitPane.getItems().addAll(chartBox, tradingBox);
        splitPane.setDividerPositions(0.7);

        return splitPane;
    }

    private VBox createChartSection() {
        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(20));
        chartBox.getStyleClass().add("chart-area");

        // Create chart with better scaling
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (hours)");
        xAxis.setAutoRanging(true);
        xAxis.setTickUnit(1);
        xAxis.setMinorTickVisible(true);
        xAxis.setMinorTickLength(3);
        xAxis.setMinorTickCount(3);  // 3 minor ticks between major ticks (15-minute intervals)

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Price ($)");
        yAxis.setAutoRanging(true);
        yAxis.setTickUnit(0.5);
        yAxis.setMinorTickVisible(true);
        yAxis.setMinorTickLength(3);
        yAxis.setMinorTickCount(4);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Stock Price History");
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(true);
        chart.getStyleClass().add("chart");

        // Set chart size
        chart.setPrefSize(800, 400);

        chartBox.getChildren().add(chart);
        return chartBox;
    }

    private VBox createTradingSection() {
        VBox tradingBox = new VBox(20);
        tradingBox.setPadding(new Insets(20));
        tradingBox.setAlignment(Pos.TOP_CENTER);

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        TextField searchField = new TextField();
        searchField.setPromptText("Enter stock symbol");
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchStock(searchField.getText()));
        searchBox.getChildren().addAll(searchField, searchBtn);

        // Stock info panel
        stockInfo = new VBox(10);
        stockInfo.getStyleClass().add("stock-info");
        stockInfo.setPadding(new Insets(20));

        // Trading type selector
        HBox tradeTypeBox = new HBox(10);
        tradeTypeBox.setAlignment(Pos.CENTER);
        Label tradeTypeLabel = new Label("Trade Type:");
        ComboBox<String> tradeTypeCombo = new ComboBox<>();
        tradeTypeCombo.getItems().addAll("Regular Trade", "Intraday Trade");
        tradeTypeCombo.setValue("Regular Trade");
        tradeTypeBox.getChildren().addAll(tradeTypeLabel, tradeTypeCombo);

        // Trading panel
        GridPane tradingPanel = new GridPane();
        tradingPanel.getStyleClass().add("trading-panel");
        tradingPanel.setHgap(10);
        tradingPanel.setVgap(10);
        tradingPanel.setPadding(new Insets(20));

        Label quantityLabel = new Label("Quantity:");
        TextField quantityField = new TextField();
        
        // Add margin target for intraday trading
        Label marginLabel = new Label("Target Margin %:");
        TextField marginField = new TextField();
        marginField.setPromptText("Enter target profit %");
        marginField.setDisable(true); // Initially disabled for regular trade

        Button buyBtn = new Button("Buy");
        Button sellBtn = new Button("Sell");

        // Enable/disable margin field based on trade type
        tradeTypeCombo.setOnAction(e -> {
            boolean isIntraday = tradeTypeCombo.getValue().equals("Intraday Trade");
            marginField.setDisable(!isIntraday);
            buyBtn.setText(isIntraday ? "Buy Intraday" : "Buy");
            sellBtn.setText(isIntraday ? "Sell Intraday" : "Sell");
        });

        buyBtn.setOnAction(e -> executeTrade(
            quantityField.getText(), 
            "BUY", 
            tradeTypeCombo.getValue(),
            marginField.getText()
        ));
        
        sellBtn.setOnAction(e -> executeTrade(
            quantityField.getText(), 
            "SELL", 
            tradeTypeCombo.getValue(),
            marginField.getText()
        ));

        tradingPanel.add(quantityLabel, 0, 0);
        tradingPanel.add(quantityField, 1, 0);
        tradingPanel.add(marginLabel, 0, 1);
        tradingPanel.add(marginField, 1, 1);
        tradingPanel.add(buyBtn, 0, 2);
        tradingPanel.add(sellBtn, 1, 2);

        tradingBox.getChildren().addAll(searchBox, stockInfo, tradeTypeBox, tradingPanel);
        return tradingBox;
    }

    private void searchStock(String symbol) {
        try {
            currentStock = stockService.fetchStockData(symbol);
            updateStockInfo();
            updateChart();
        } catch (Exception e) {
            showError("Error fetching stock data: " + e.getMessage());
        }
    }

    private void executeTrade(String quantityStr, String type, String tradeType, String marginStr) {
        try {
            if (currentStock == null) {
                showError("Please search for a stock first");
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            double totalCost = quantity * currentStock.getPrice();
            User currentUser = userService.getCurrentUser();
            
            // Handle intraday trading
            boolean isIntraday = tradeType.equals("Intraday Trade");
            double margin = 0.0;
            
            if (isIntraday) {
                if (marginStr == null || marginStr.trim().isEmpty()) {
                    showError("Please enter target margin for intraday trading");
                    return;
                }
                try {
                    margin = Double.parseDouble(marginStr);
                } catch (NumberFormatException e) {
                    showError("Invalid margin percentage");
                    return;
                }
            }

            if (type.equals("BUY")) {
                if (totalCost > currentUser.getBalance()) {
                    showError("Insufficient funds");
                    return;
                }
                currentUser.setBalance(currentUser.getBalance() - totalCost);
                
                // Create a new stock for the portfolio
                Stock portfolioStock = new Stock(currentStock.getSymbol(), currentStock.getPrice(), currentStock.getChange());
                portfolioStock.setQuantity(quantity);
                portfolioStock.setIntraday(isIntraday);
                if (isIntraday) {
                    portfolioStock.setTargetMargin(margin);
                }
                
                // Save to database
                Transaction transaction = new Transaction(
                    currentStock.getSymbol(),
                    quantity,
                    currentStock.getPrice(),
                    "BUY",
                    isIntraday,
                    margin
                );
                portfolioService.saveTransaction(transaction, currentUser.getUsername());
                portfolioService.updatePortfolio(currentUser.getUsername(), portfolioStock);
                
            } else {
                // Check if user has enough stocks to sell
                Stock portfolioStock = currentUser.getPortfolio().stream()
                    .filter(s -> s.getSymbol().equals(currentStock.getSymbol()))
                    .findFirst()
                    .orElse(null);

                if (portfolioStock == null || portfolioStock.getQuantity() < quantity) {
                    showError("Insufficient stocks to sell");
                    return;
                }

                currentUser.setBalance(currentUser.getBalance() + totalCost);
                
                // Save to database
                Transaction transaction = new Transaction(
                    currentStock.getSymbol(),
                    quantity,
                    currentStock.getPrice(),
                    "SELL",
                    isIntraday,
                    margin
                );
                portfolioService.saveTransaction(transaction, currentUser.getUsername());
                portfolioService.removeFromPortfolio(currentUser.getUsername(), currentStock.getSymbol(), quantity);
            }
            
            updateBalanceLabel();
            showSuccess(type + " order executed successfully");
            
        } catch (Exception e) {
            showError("Error executing trade: " + e.getMessage());
        }
    }

    private void updateStockInfo() {
        stockInfo.getChildren().clear();
        stockInfo.setSpacing(10);
        stockInfo.setPadding(new Insets(20));

        // Stock Symbol
        Label symbolLabel = new Label(currentStock.getSymbol());
        symbolLabel.getStyleClass().add("stock-symbol");

        // Stock Price
        Label priceLabel = new Label(String.format("$%.2f", currentStock.getPrice()));
        priceLabel.getStyleClass().add("stock-price");

        // Price Change
        Label changeLabel = new Label(String.format("%.2f%%", currentStock.getChange()));
        changeLabel.getStyleClass().addAll("stock-change", 
            currentStock.getChange() >= 0 ? "positive-change" : "negative-change");

        // Add labels to the panel
        stockInfo.getChildren().addAll(symbolLabel, priceLabel, changeLabel);
    }

    private void updateChart() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(currentStock.getSymbol());
        
        // Add data points for 24 hours
        double basePrice = currentStock.getPrice();
        for (int i = 0; i < 24; i++) {  // Changed to 24 hours
            // Price variation for hourly data
            double price = basePrice + (Math.random() * 5 - 2.5);
            series.getData().add(new XYChart.Data<>(i, price));
        }

        chart.getData().clear();
        chart.getData().add(series);

        // Auto-adjust the Y-axis range with less padding
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        double min = series.getData().stream().mapToDouble(d -> d.getYValue().doubleValue()).min().orElse(0);
        double max = series.getData().stream().mapToDouble(d -> d.getYValue().doubleValue()).max().orElse(0);
        double padding = (max - min) * 0.05;
        yAxis.setLowerBound(min - padding);
        yAxis.setUpperBound(max + padding);
    }

    private void startRealTimeUpdates() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentStock != null) {
                    Platform.runLater(() -> {
                        try {
                            // Fetch real-time data
                            currentStock = stockService.fetchStockData(currentStock.getSymbol());
                            
                            // Update UI
                            updateStockInfo();
                            updateChart();
                            
                            // Update portfolio if open
                            if (portfolioStage != null && portfolioStage.isShowing()) {
                                showPortfolio();
                            }
                            
                            // Update history if open
                            if (historyStage != null && historyStage.isShowing()) {
                                showHistory();
                            }
                            
                            // Update balance label
                            updateBalanceLabel();
                            
                        } catch (Exception e) {
                            showError("Error updating stock data: " + e.getMessage());
                        }
                    });
                }
            }
        }, 0, 15000); // Update every 15 seconds (API rate limit consideration)
    }

    private void showPortfolio() {
        try {
            User currentUser = userService.getCurrentUser();
            List<Stock> portfolio = portfolioService.getPortfolio(currentUser.getUsername());
            
            // Update portfolio display
            Stage portfolioStage = new Stage();
            portfolioStage.setTitle("Portfolio");
            
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("portfolio-view");

            // Add total portfolio value label
            double totalValue = portfolio.stream()
                .mapToDouble(s -> s.getQuantity() * s.getPrice())
                .sum();
            Label totalValueLabel = new Label(String.format("Total Portfolio Value: $%.2f", totalValue));
            totalValueLabel.getStyleClass().add("portfolio-total");

            TableView<Stock> portfolioTable = new TableView<>();
            portfolioTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Symbol Column
            TableColumn<Stock, String> symbolCol = new TableColumn<>("Symbol");
            symbolCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSymbol()));

            // Type Column
            TableColumn<Stock, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isIntraday() ? "Intraday" : "Regular"
            ));

            // Quantity Column
            TableColumn<Stock, Number> quantityCol = new TableColumn<>("Quantity");
            quantityCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()));

            // Price Column
            TableColumn<Stock, Number> priceCol = new TableColumn<>("Current Price");
            priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrice()));
            priceCol.setCellFactory(col -> new TableCell<Stock, Number>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", value.doubleValue()));
                    }
                }
            });

            // Value Column
            TableColumn<Stock, Number> valueCol = new TableColumn<>("Total Value");
            valueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                data.getValue().getQuantity() * data.getValue().getPrice()
            ));
            valueCol.setCellFactory(col -> new TableCell<Stock, Number>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", value.doubleValue()));
                    }
                }
            });

            // Change Column
            TableColumn<Stock, Number> changeCol = new TableColumn<>("Change %");
            changeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getChange()));
            changeCol.setCellFactory(col -> new TableCell<Stock, Number>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        double change = value.doubleValue();
                        setText(String.format("%.2f%%", change));
                        setTextFill(change >= 0 ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
                    }
                }
            });

            // Target Margin Column (for intraday)
            TableColumn<Stock, Number> targetMarginCol = new TableColumn<>("Target Margin %");
            targetMarginCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                data.getValue().isIntraday() ? data.getValue().getTargetMargin() : 0.0
            ));
            targetMarginCol.setCellFactory(col -> new TableCell<Stock, Number>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(value.doubleValue() > 0 ? String.format("%.1f%%", value.doubleValue()) : "-");
                    }
                }
            });

            portfolioTable.getColumns().addAll(
                symbolCol, typeCol, quantityCol, priceCol, valueCol, changeCol, targetMarginCol
            );

            // Add data to table
            portfolioTable.getItems().addAll(portfolio);

            // Add refresh button
            Button refreshBtn = new Button("Refresh Portfolio");
            refreshBtn.setOnAction(e -> {
                portfolioTable.getItems().clear();
                portfolioTable.getItems().addAll(portfolio);
                double newTotalValue = portfolio.stream()
                    .mapToDouble(s -> s.getQuantity() * s.getPrice())
                    .sum();
                totalValueLabel.setText(String.format("Total Portfolio Value: $%.2f", newTotalValue));
            });

            content.getChildren().addAll(totalValueLabel, portfolioTable, refreshBtn);
            
            // Add styles
            content.getStyleClass().add("portfolio-content");
            portfolioTable.getStyleClass().add("portfolio-table");
            refreshBtn.getStyleClass().add("refresh-button");
            
            Scene scene = new Scene(content, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            portfolioStage.setScene(scene);
            portfolioStage.show();
        } catch (SQLException e) {
            showError("Error loading portfolio: " + e.getMessage());
        }
    }

    private void showHistory() {
        try {
            User currentUser = userService.getCurrentUser();
            List<Transaction> transactions = portfolioService.getTransactionHistory(currentUser.getUsername());
            
            // Update transaction history display
            Stage historyStage = new Stage();
            historyStage.setTitle("Transaction History");

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("portfolio-content");

            // Add total transactions value label
            double totalValue = transactions.stream()
                .mapToDouble(t -> t.getQuantity() * t.getPrice() * (t.getType().equals("SELL") ? 1 : -1))
                .sum();
            Label totalValueLabel = new Label(String.format("Total Trading Volume: $%.2f", Math.abs(totalValue)));
            totalValueLabel.getStyleClass().add("portfolio-total");

            TableView<Transaction> historyTable = new TableView<>();
            historyTable.getStyleClass().add("portfolio-table");
            historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Date/Time Column
            TableColumn<Transaction, String> timeCol = new TableColumn<>("Date/Time");
            timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));

            // Symbol Column
            TableColumn<Transaction, String> symbolCol = new TableColumn<>("Symbol");
            symbolCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSymbol()));

            // Type Column with colored text
            TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
            typeCol.setCellFactory(col -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String type, boolean empty) {
                    super.updateItem(type, empty);
                    if (empty || type == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(type);
                        setTextFill(type.equals("BUY") ? Color.GREEN : Color.RED);
                    }
                }
            });

            // Trade Type Column
            TableColumn<Transaction, String> tradeTypeCol = new TableColumn<>("Trade Type");
            tradeTypeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isIntraday() ? "Intraday" : "Regular"
            ));

            // Quantity Column
            TableColumn<Transaction, Number> quantityCol = new TableColumn<>("Quantity");
            quantityCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()));

            // Price Column
            TableColumn<Transaction, Number> priceCol = new TableColumn<>("Price");
            priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrice()));
            priceCol.setCellFactory(col -> new TableCell<Transaction, Number>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", value.doubleValue()));
                    }
                }
            });

            // Total Value Column
            TableColumn<Transaction, Number> valueCol = new TableColumn<>("Total Value");
            valueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                data.getValue().getQuantity() * data.getValue().getPrice()
            ));
            valueCol.setCellFactory(col -> new TableCell<Transaction, Number>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", value.doubleValue()));
                    }
                }
            });

            // Target Margin Column (for intraday)
            TableColumn<Transaction, Number> targetMarginCol = new TableColumn<>("Target Margin %");
            targetMarginCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                data.getValue().isIntraday() ? data.getValue().getTargetMargin() : 0.0
            ));
            targetMarginCol.setCellFactory(col -> new TableCell<Transaction, Number>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(value.doubleValue() > 0 ? String.format("%.1f%%", value.doubleValue()) : "-");
                    }
                }
            });

            historyTable.getColumns().addAll(
                timeCol, symbolCol, typeCol, tradeTypeCol, quantityCol, priceCol, valueCol, targetMarginCol
            );

            // Add data to table
            historyTable.getItems().addAll(transactions);

            // Add refresh button
            Button refreshBtn = new Button("Refresh History");
            refreshBtn.getStyleClass().add("refresh-button");
            refreshBtn.setOnAction(e -> {
                historyTable.getItems().clear();
                historyTable.getItems().addAll(transactions);
                double newTotalValue = transactions.stream()
                    .mapToDouble(t -> t.getQuantity() * t.getPrice() * (t.getType().equals("SELL") ? 1 : -1))
                    .sum();
                totalValueLabel.setText(String.format("Total Trading Volume: $%.2f", Math.abs(newTotalValue)));
            });

            content.getChildren().addAll(totalValueLabel, historyTable, refreshBtn);

            Scene scene = new Scene(content, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

            historyStage.setScene(scene);
            historyStage.show();
        } catch (SQLException e) {
            showError("Error loading transaction history: " + e.getMessage());
        }
    }

    private void updateBalanceLabel() {
        balanceLabel.setText(String.format("Balance: $%.2f", userService.getCurrentUser().getBalance()));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleLogout() {
        // Save current user data
        userService.logout();
        
        // Clear current state
        currentStock = null;
        if (chart != null) {
            chart.getData().clear();
        }
        
        // Reset UI elements
        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
        root.getChildren().clear();
        
        // Show login screen
        createLoginScreen(root);
        
        // Update window title
        primaryStage.setTitle("Stock Market Simulator - Login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}