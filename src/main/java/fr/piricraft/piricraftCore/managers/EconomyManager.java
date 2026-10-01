package fr.piricraft.piricraftCore.managers;

import fr.piricraft.piricraftCore.models.PlayerProfile;

import java.util.UUID;

public class EconomyManager {

    private final DatabaseManager databaseManager;

    public EconomyManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public synchronized double getBalance(UUID uuid) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        return profile != null ? profile.getBalance() : 0.0;
    }

    public synchronized boolean hasMoney(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        return profile != null && profile.hasEnoughMoney(amount);
    }

    public synchronized void depositMoney(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        if (profile != null) {
            profile.credit(amount);
            databaseManager.saveProfileAsync(profile);
        }
    }

    public synchronized boolean withdrawMoney(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        if (profile == null || !profile.debit(amount)) {
            return false;
        }
        databaseManager.saveProfileAsync(profile);
        return true;
    }

    public synchronized void setBalance(UUID uuid, double amount) {
        PlayerProfile profile = databaseManager.getProfileFromCache(uuid);
        if (profile != null) {
            profile.setBalance(amount);
            databaseManager.saveProfileAsync(profile);
        }
    }

    public synchronized boolean transferMoney(UUID fromUuid, UUID toUuid, double amount) {
        if (fromUuid.equals(toUuid) || !Double.isFinite(amount) || amount <= 0) {
            return false;
        }
        PlayerProfile from = databaseManager.getProfileFromCache(fromUuid);
        PlayerProfile to = databaseManager.getProfileFromCache(toUuid);
        if (from == null || to == null || !from.hasEnoughMoney(amount) || !Double.isFinite(to.getBalance() + amount)) {
            return false;
        }
        if (!from.debit(amount)) {
            return false;
        }
        to.credit(amount);
        databaseManager.saveProfilesAsync(from, to);
        return true;
    }
}
