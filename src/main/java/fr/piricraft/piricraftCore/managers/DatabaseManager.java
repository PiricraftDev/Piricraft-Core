package fr.piricraft.piricraftCore.managers;

import fr.piricraft.piricraftCore.PiricraftCore;
import fr.piricraft.piricraftCore.models.PlayerProfile;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {

    private final PiricraftCore plugin;
    private final Map<UUID, PlayerProfile> profileCache = new ConcurrentHashMap<>();
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "PiricraftCore-Database");
        thread.setDaemon(true);
        return thread;
    });
    private volatile Connection connection;

    public DatabaseManager(PiricraftCore plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new SQLException("Could not create plugin data folder");
            }

            File dbFile = new File(plugin.getDataFolder(), "database.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA busy_timeout = 5000");
                statement.execute("PRAGMA journal_mode = WAL");
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS piricraft_players (
                        uuid VARCHAR(36) PRIMARY KEY,
                        name VARCHAR(16) NOT NULL,
                        balance DOUBLE NOT NULL,
                        first_join BIGINT NOT NULL
                    )
                    """);
            }
            plugin.getLogger().info("Base de données SQLite initialisée avec succès.");
        } catch (SQLException e) {
            databaseExecutor.shutdownNow();
            throw new IllegalStateException("Impossible d'initialiser la base de données SQLite.", e);
        }
    }

    public CompletableFuture<PlayerProfile> loadProfileAsync(UUID uuid, String name) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT name, balance, first_join FROM piricraft_players WHERE uuid = ?";
            try (PreparedStatement statement = getConnection().prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        PlayerProfile profile = new PlayerProfile(uuid, name, result.getDouble("balance"), result.getLong("first_join"));
                        profileCache.put(uuid, profile);
                        if (!name.equals(result.getString("name"))) {
                            saveProfileAsync(profile);
                        }
                        return profile;
                    }
                }

                PlayerProfile profile = new PlayerProfile(uuid, name, 100.0);
                insertNewProfile(profile);
                profileCache.put(uuid, profile);
                return profile;
            } catch (SQLException e) {
                throw new IllegalStateException("Impossible de charger le profil " + uuid, e);
            }
        }, databaseExecutor);
    }

    private void insertNewProfile(PlayerProfile profile) throws SQLException {
        String insert = "INSERT INTO piricraft_players (uuid, name, balance, first_join) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(insert)) {
            statement.setString(1, profile.getPlayerUuid().toString());
            statement.setString(2, profile.getPlayerName());
            statement.setDouble(3, profile.getBalance());
            statement.setLong(4, profile.getFirstJoinTimestamp());
            statement.executeUpdate();
        }
    }

    public CompletableFuture<Void> saveProfileAsync(PlayerProfile profile) {
        if (profile == null) {
            return CompletableFuture.completedFuture(null);
        }

        UUID uuid = profile.getPlayerUuid();
        String name = profile.getPlayerName();
        double balance = profile.getBalance();
        return CompletableFuture.runAsync(() -> {
            String update = "UPDATE piricraft_players SET name = ?, balance = ? WHERE uuid = ?";
            try (PreparedStatement statement = getConnection().prepareStatement(update)) {
                statement.setString(1, name);
                statement.setDouble(2, balance);
                statement.setString(3, uuid.toString());
                if (statement.executeUpdate() == 0) {
                    try (PreparedStatement insert = getConnection().prepareStatement(
                            "INSERT INTO piricraft_players (uuid, name, balance, first_join) VALUES (?, ?, ?, ?)")) {
                        insert.setString(1, uuid.toString());
                        insert.setString(2, name);
                        insert.setDouble(3, balance);
                        insert.setLong(4, profile.getFirstJoinTimestamp());
                        insert.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Impossible d'enregistrer le profil " + uuid, e);
            }
        }, databaseExecutor);
    }

    public CompletableFuture<Void> saveProfilesAsync(PlayerProfile first, PlayerProfile second) {
        if (first == null || second == null) {
            return CompletableFuture.completedFuture(null);
        }
        String[][] profiles = {
                {first.getPlayerUuid().toString(), first.getPlayerName(), Double.toString(first.getBalance()), Long.toString(first.getFirstJoinTimestamp())},
                {second.getPlayerUuid().toString(), second.getPlayerName(), Double.toString(second.getBalance()), Long.toString(second.getFirstJoinTimestamp())}
        };
        return CompletableFuture.runAsync(() -> {
            Connection current = null;
            try {
                current = getConnection();
                current.setAutoCommit(false);
                try (PreparedStatement update = current.prepareStatement("UPDATE piricraft_players SET name = ?, balance = ? WHERE uuid = ?");
                     PreparedStatement insert = current.prepareStatement("INSERT INTO piricraft_players (uuid, name, balance, first_join) VALUES (?, ?, ?, ?)")) {
                    for (String[] profile : profiles) {
                        update.setString(1, profile[1]);
                        update.setDouble(2, Double.parseDouble(profile[2]));
                        update.setString(3, profile[0]);
                        if (update.executeUpdate() == 0) {
                            insert.setString(1, profile[0]);
                            insert.setString(2, profile[1]);
                            insert.setDouble(3, Double.parseDouble(profile[2]));
                            insert.setLong(4, Long.parseLong(profile[3]));
                            insert.executeUpdate();
                        }
                    }
                    current.commit();
                } catch (SQLException | NumberFormatException e) {
                    current.rollback();
                    throw e;
                } finally {
                    current.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Impossible d'enregistrer le transfert des profils", e);
            }
        }, databaseExecutor);
    }

    public void saveAllProfilesAsync() {
        for (PlayerProfile profile : profileCache.values()) {
            saveProfileAsync(profile);
        }
    }

    public void unloadProfile(UUID uuid) {
        PlayerProfile profile = profileCache.get(uuid);
        if (profile != null) {
            saveProfileAsync(profile).whenComplete((ignored, error) -> {
                if (error != null) {
                    plugin.getLogger().severe("Échec de sauvegarde du profil " + uuid + " : " + error.getMessage());
                }
                profileCache.remove(uuid, profile);
            });
        }
    }

    public PlayerProfile getProfileFromCache(UUID uuid) {
        return profileCache.get(uuid);
    }

    public void closeConnection() {
        saveAllProfilesAsync();
        databaseExecutor.shutdown();
        try {
            if (!databaseExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                databaseExecutor.shutdownNow();
                if (!databaseExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    plugin.getLogger().severe("Les opérations de base de données n'ont pas pu se terminer à temps.");
                }
            }
            Connection current = connection;
            if (current != null && !current.isClosed()) {
                current.close();
            }
        } catch (InterruptedException e) {
            databaseExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            plugin.getLogger().severe("Arrêt interrompu pendant la sauvegarde de la base de données.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Impossible de fermer la base SQLite : " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        Connection current = connection;
        if (current == null || current.isClosed()) {
            throw new SQLException("La connexion SQLite n'est pas disponible");
        }
        return current;
    }
}
