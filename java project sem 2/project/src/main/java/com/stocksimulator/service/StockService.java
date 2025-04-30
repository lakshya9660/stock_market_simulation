package com.stocksimulator.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.stocksimulator.model.Stock;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockService {
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build();
        
    private static final String API_KEY = "8885b40d6ae140e5f6610e001156e15b"; // Replace with your Marketstack API key
    private static final String BASE_URL = "http://api.marketstack.com/v1";

    public Stock fetchStockData(String symbol) throws IOException {
        String url = String.format("%s/eod/latest?access_key=%s&symbols=%s", 
            BASE_URL, API_KEY, symbol);

        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch stock data: " + response);
            }

            String jsonData = response.body().string();
            JSONObject json = new JSONObject(jsonData);
            
            // Check for API errors
            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                throw new IOException("API Error: " + error.getString("message"));
            }

            // Parse Marketstack response format
            if (json.has("data") && json.getJSONArray("data").length() > 0) {
                JSONObject data = json.getJSONArray("data").getJSONObject(0);
                double price = data.getDouble("close");
                double previousClose = data.getDouble("open");
                double change = ((price - previousClose) / previousClose) * 100;

                return new Stock(symbol, price, change);
            } else {
                throw new IOException("No data found for symbol: " + symbol);
            }
        }
    }

    public List<Stock> getPopularStocks() throws IOException {
        List<Stock> stocks = new ArrayList<>();
        String[] symbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "META"}; // Popular tech stocks

        for (String symbol : symbols) {
            try {
                stocks.add(fetchStockData(symbol));
            } catch (IOException e) {
                System.err.println("Error fetching " + symbol + ": " + e.getMessage());
            }
        }

        return stocks;
    }
}