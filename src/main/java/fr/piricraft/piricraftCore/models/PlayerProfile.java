package fr.piricraft.piricraftCore.models;

import java.util.UUID;

public class PlayerProfile {

    private final UUID playerUuid;
    private String playerName;
    private double balance;
    private final long firstJoinTimestamp;

    // Constructor
    public PlayerProfile(UUID playerUuid, String playerName, double balance, long firstJoinTimestamp) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.balance = Double.isFinite(balance) && balance >= 0 ? balance : 0;
        this.firstJoinTimestamp = firstJoinTimestamp;
    }

    // Constructor for a new player
    public PlayerProfile(UUID playerUuid, String playerName, double defaultBalance) {
        this(playerUuid, playerName, defaultBalance, System.currentTimeMillis());
    }

    // Getters
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public long getFirstJoinTimestamp() {
        return firstJoinTimestamp;
    }

    // Setters
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public synchronized void setBalance(double balance) {
        if (Double.isFinite(balance) && balance >= 0) {
            this.balance = balance;
        }
    }

    // Method about balance
    public synchronized boolean hasEnoughMoney(double amount) {
        return Double.isFinite(amount) && amount >= 0 && this.balance >= amount;
    }

    public synchronized void credit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0 || !Double.isFinite(this.balance + amount)) return;
        this.balance += amount;
    }

    public synchronized boolean debit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0 || this.balance < amount) {
            return false;
        }
        this.balance -= amount;
        return true;
    }
}

