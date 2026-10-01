package fr.piricraft.piricraftCore.managers;

import fr.piricraft.piricraftCore.models.PlayerProfile;

import java.util.UUID;

public class EconomyManager {

    private final DatabaseManager databaseManager;

    public EconomyManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public double getBalance(UUID uuid) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        return profile != null ? profile.getBalance() : 0.0;
    }

    public boolean hasMoney(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        return profile != null && profile.hasEnoughMoney(amount);
    }

    public void depositMoney(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        if (profile != null) {
            profile.credit(amount);
        }
    }

    public boolean withdrawMoney(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        return profile != null && profile.debit(amount);
    }

    public void setBalance(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        if (profile != null) {
            profile.setBalance(amount);
        }
    }
}