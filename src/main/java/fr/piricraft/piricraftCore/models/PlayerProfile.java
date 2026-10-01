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
        this.balance = balance;
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

    public double getBalance() {
        return balance;
    }

    public long getFirstJoinTimestamp() {
        return firstJoinTimestamp;
    }

    // Setters
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // Method about balance
    public boolean hasEnoughMoney(double amount) {
        return this.balance >= amount;
    }

    public void credit(double amount) {
        if (amount <= 0) return;
        this.balance += amount;
    }

    public boolean debit(double amount) {
        if (amount <= 0 || !hasEnoughMoney(amount)) {
            return false;
        }
        this.balance -= amount;
        return true;
    }
}

