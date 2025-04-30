# Stock Market Simulator

A Java-based stock market simulator application that allows users to simulate trading stocks with real-time market data.

## Features

- **User Authentication**
  - Login and registration system
  - Secure password storage
  - User session management

- **Stock Trading**
  - Real-time stock price updates
  - Buy and sell stocks
  - Intraday trading support
  - Target margin trading
  - Transaction history tracking

- **Portfolio Management**
  - View current holdings
  - Track portfolio value
  - Monitor profit/loss
  - Detailed transaction history

- **Market Data**
  - Real-time stock price charts
  - Historical price data
  - Stock search functionality
  - Price change indicators

## Technical Stack

- **Language**: Java 17
- **Framework**: JavaFX 17.0.2
- **Build Tool**: Maven
- **Dependencies**:
  - JavaFX Controls and FXML
  - JSON for data handling
  - OkHttp for API calls

## Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher
- Internet connection for real-time stock data

## Database Setup

### Prerequisites
1. MySQL Server (version 8.0 or higher)
2. MySQL Connector/J (included in project dependencies)

### Installation Steps

1. **Install MySQL Server**
   - Download MySQL Server from [MySQL Official Website](https://dev.mysql.com/downloads/mysql/)
   - Follow the installation instructions for your operating system
   - During installation, set a root password (remember this password)

2. **Configure Database**
   - Open MySQL Command Line Client or MySQL Workbench
   - Log in using your root credentials
   - The application will automatically create the database and required tables on first run

3. **Update Database Configuration**
   - Open `src/main/java/com/stocksimulator/config/DatabaseConfig.java`
   - Update the following configuration if needed:
     ```java
     private static final String URL = "jdbc:mysql://localhost:3306/stock_simulator";
     private static final String USER = "root";
     private static final String PASSWORD = "your_password_here"; // Change this to your MySQL password
     ```

### Database Schema

The application uses two main tables:

1. **transactions**
   - Stores all trading transactions
   - Fields: id, username, symbol, quantity, price, type, timestamp, is_intraday, target_margin

2. **portfolio**
   - Stores user's current stock holdings
   - Fields: username, symbol, quantity, price, is_intraday, target_margin

### Troubleshooting

If you encounter database connection issues:

1. Ensure MySQL Server is running
2. Verify the database credentials in DatabaseConfig.java
3. Check if the MySQL service is running on port 3306
4. Make sure you have the correct MySQL Connector/J version in your pom.xml

## Installation

1. Clone the repository:
   ```bash
   git clone [repository-url]
   ```

2. Navigate to the project directory:
   ```bash
   cd stock-simulator
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

## Running the Application

1. Make sure MySQL Server is running
2. Build the project using Maven:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn javafx:run
   ```

## Project Structure

```
src/main/java/com/stocksimulator/
├── Launcher.java           # Application entry point
├── StockSimulatorApp.java  # Main application class
├── model/                  # Data models
│   ├── Stock.java
│   ├── Transaction.java
│   └── User.java
└── service/                # Business logic
    ├── StockService.java
    └── UserService.java
```

## Usage

1. **Registration/Login**
   - Create a new account or login with existing credentials
   - Initial balance: $100,000

2. **Trading**
   - Search for stocks using their symbols
   - View real-time price charts
   - Execute buy/sell orders
   - Set target margins for trades
   - Monitor your portfolio

3. **Portfolio Management**
   - View your current holdings
   - Track your investment performance
   - Review transaction history

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- JavaFX for the UI framework
- Maven for build automation
- OkHttp for API communication

## Additional Information

- The application uses MySQL Connector/J version 8.0.33
- Database is automatically created on first run
- All database operations are handled through the PortfolioService class 