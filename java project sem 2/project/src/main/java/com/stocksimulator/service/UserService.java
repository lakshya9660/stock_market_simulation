package com.stocksimulator.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.stocksimulator.model.User;

public class UserService {
    private static final String USERS_FILE = Paths.get(System.getProperty("user.home"), "stock_simulator_users.dat").toString();
    private Map<String, User> users;
    private User currentUser;

    public UserService() {
        users = new HashMap<>();
        loadUsers();
        System.out.println("Users file path: " + USERS_FILE);
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        User newUser = new User(username, password);
        users.put(username, newUser);
        currentUser = newUser;
        saveUsers();
        System.out.println("Registered new user: " + username);
        return true;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            System.out.println("Invalid username or password format");
            return false;
        }
        
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            System.out.println("Logged in user: " + username);
            System.out.println("User balance: " + user.getBalance());
            System.out.println("User portfolio size: " + user.getPortfolio().size());
            System.out.println("User transactions: " + user.getTransactions().size());
            return true;
        }
        System.out.println("Login failed for user: " + username);
        return false;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("Logging out user: " + currentUser.getUsername());
            saveUsers();
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("Users file does not exist, starting fresh");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                users = (Map<String, User>) obj;
                System.out.println("Loaded " + users.size() + " users from file");
            } else {
                System.out.println("Invalid data format in users file, starting fresh");
                users = new HashMap<>();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found, starting fresh");
            users = new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            users = new HashMap<>();
        }
    }

    public void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            System.out.println("Saved " + users.size() + " users to file");
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 