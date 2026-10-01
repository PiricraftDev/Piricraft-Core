# Cahier des Charges Technique — Piricraft-Core

Noyau central et infrastructure de données du serveur Piricraft. Ce plugin prend en charge la persistance asynchrone des profils joueurs, la gestion de l'économie interne et un framework d'interfaces graphiques (GUI). Il fournit une API interne exploitée par les futurs modules (`Piricraft-Jobs` et `Piricraft-Essential`).

---

## 1. Spécifications Techniques

* **Version Minecraft Target :** Édition Java 26.2 (Paper API)
* **Version Java :** Java 25
* **Dépendances Externe :** Aucune (Système d'économie et GUI 100% natifs)
* **Système de Build :** Gradle (Kotlin DSL) / Maven

---

## 2. Découpage Chronologique & Spécifications des Classes

### Étape 1 — Structure de Données & Persistance (Noyau)

#### 1. `models/PlayerProfile.java`
* **Rôle :** Modèle de données représentant le profil d'un joueur connecté en mémoire RAM.
* **Attributs :**
    * `private final UUID playerUuid` : Identifiant unique et immuable du joueur.
    * `private String playerName` : Dernier pseudo connu du joueur en jeu.
    * `private double balance` : Solde d'argent actuel du joueur.
    * `private final long firstJoinTimestamp` : Date/Heure de première connexion (`System.currentTimeMillis()`).
* **Méthodes clés :**
    * Constructeur complet et constructeur pour nouveau joueur.
    * Getters et Setters pour `playerName` et `balance`.
    * Méthodes utilitaires : `hasEnoughMoney(double amount)`, `credit(double amount)`, `debit(double amount)`.

#### 2. `managers/DatabaseManager.java`
* **Rôle :** Orchestrateur des connexions SQL et de la persistance SQLite/MySQL.
* **Fonctionnalités :**
    * Initialisation du pool de connexions (HikariCP ou SQLite via JDBC).
    * `initDatabase()` : Exécute la requête `CREATE TABLE IF NOT EXISTS piricraft_players (...)`.
    * `loadProfileAsync(UUID uuid, String name)` : Interroge la BDD en asynchrone. Si le profil n'existe pas, il insère une nouvelle ligne avec le solde de départ paramétré.
    * `saveProfileAsync(PlayerProfile profile)` : Sauvegarde l'état actuel d'un `PlayerProfile` en BDD (`UPDATE`).
    * `saveAllProfilesAsync()` : Parcourt la HashMap des profils en mémoire et les sauvegarde tous (exécuté lors de l'auto-save et du `onDisable`).
    * **Stockage local :** `private final Map<UUID, PlayerProfile> profileCache = new ConcurrentHashMap<>();`

#### 3. `listeners/PlayerConnectionListener.java`
* **Rôle :** Synchronisation du cycle de vie du joueur avec la mémoire et la base de données.
* **Événements interceptés :**
    * `AsyncPlayerPreLoginEvent` : Exécute `DatabaseManager#loadProfileAsync` pour garantir que les données du joueur sont chargées en mémoire *avant* qu'il n'apparaisse dans le monde.
    * `PlayerQuitEvent` : Exécute `DatabaseManager#saveProfileAsync` puis retire l'objet de la `ConcurrentHashMap` afin de libérer la mémoire RAM.

#### 4. `PiricraftCore.java` *(Classe Principale)*
* **Rôle :** Point d'entrée principal (`JavaPlugin`).
* **Cycle de vie :**
    * `onEnable()` : Instancie `DatabaseManager`, exécute `initDatabase()`, enregistre les listeners et planifie une tâche asynchrone récurrente (`BukkitRunnable`) de sauvegarde toutes les 10 minutes.
    * `onDisable()` : Annule les tâches planifiées, force la sauvegarde globale via `saveAllProfilesAsync()` et ferme la connexion SQL proprement.

---

### Étape 2 — Logique Économique & Commandes

#### 5. `managers/EconomyManager.java`
* **Rôle :** Service de gestion transactionnelle de la monnaie du serveur.
* **Lien :** Interagit directement avec la HashMap de `PlayerProfile` stockée dans `DatabaseManager`.
* **Méthodes clés :**
    * `double getBalance(UUID uuid)` : Renvoie le solde actuel ou `0.0` si non trouvé.
    * `boolean hasMoney(UUID uuid, double amount)` : Vérifie si le joueur possède au moins `amount`.
    * `boolean depositMoney(UUID uuid, double amount)` : Ajoute la somme au profil.
    * `boolean withdrawMoney(UUID uuid, double amount)` : Débite le joueur (renvoie `false` si le solde est insuffisant).
    * `void setBalance(UUID uuid, double amount)` : Remplace le solde par une valeur exacte.

#### 6. `commands/MoneyCommand.java`
* **Rôle :** Permet la consultation du solde personnel ou d'un autre joueur.
* **Exécution :** `/bal` ou `/money` `[joueur]`
* **Logique :** Récupère la valeur via `EconomyManager` et la renvoie sous forme de message formaté.

#### 7. `commands/PayCommand.java`
* **Rôle :** Permet aux joueurs de transférer de la monnaie entre eux.
* **Exécution :** `/pay <joueur> <montant>`
* **Sécurités & Règles :**
    * Le montant doit être strictement supérieur à zero (`amount > 0`).
    * Empêcher les auto-transferts (`sender != target`).
    * Vérification du solde de l'émetteur via `EconomyManager#hasMoney`.
    * Transaction atomique : Débit du joueur A suivi du crédit du joueur B.

#### 8. `commands/EcoAdminCommand.java`
* **Rôle :** Commande de modération/administration de l'économie.
* **Permission :** `piricraft.admin.eco`
* **Exécution :** `/eco <give|take|set> <joueur> <montant>`
* **Logique :** Exécute directement les modifications de solde sur `EconomyManager` quel que soit l'état du joueur.

---

### Étape 3 — Framework GUI (Interfaces Graphiques)

#### 9. `models/PiricraftMenu.java`
* **Rôle :** Classe abstraite implémentant `InventoryHolder` servant de modèle de base à tout menu interactif.
* **Attributs :**
    * `protected Inventory inventory;`
    * `protected String title;`
    * `protected int slots;`
* **Méthodes clés :**
    * `public abstract void setMenuItems(Player player);` : Définit la disposition des objets dans l'inventaire.
    * `public abstract void onClick(InventoryClickEvent event);` : Définit le comportement au clic.
    * `public void open(Player player);` : Ouvre l'inventaire pour le joueur donné.

#### 10. `managers/MenuManager.java`
* **Rôle :** Usine à fabriquer des objets (`ItemStack`) et gestionnaire de menus.
* **Fonctionnalités :**
    * Méthode utilitaire `createItem(Material mat, Component name, List<Component> lore, boolean enchanted)` : Génère un `ItemStack` prêt à l'emploi.
    * Gestion des têtes de joueurs personnalisées (`PLAYER_HEAD`).

#### 11. `listeners/MenuListener.java`
* **Rôle :** Écouteur global intercepteur d'interactions dans les GUI.
* **Événements interceptés :**
    * `InventoryClickEvent` : Vérifie si le `InventoryHolder` de l'inventaire supérieur est une instance de `PiricraftMenu`. Si c'est le cas, il annule systématiquement l'événement (`event.setCancelled(true)`) pour empêcher la récupération d'items et délègue l'action à la méthode `onClick()` du menu.

---

### Étape 4 — API Publique Interne & Utilitaires

#### 12. `api/PiricraftCoreAPI.java`
* **Rôle :** Pont d'accès statique permettant aux plugins tiers/internes d'accéder aux fonctionnalités du noyau.
* **Méthodes statiques :**
    * `public static EconomyManager getEconomy()`
    * `public static MenuManager getMenuManager()`
    * `public static DatabaseManager getDatabase()`

#### 13. `utils/TextUtils.java`
* **Rôle :** Formatage et traitement des chaînes de caractères.
* **Fonctionnalités :**
    * Utilisation native de l'API `MiniMessage` d'Adventure (Paper API).
    * `public static Component color(String text)` : Convertit les balises MiniMessage (ex: `<green>Text</green>`) en `Component`.

#### 14. `utils/NumberUtils.java`
* **Rôle :** Formatage visuel des valeurs numériques et monétaires.
* **Fonctionnalités :**
    * `public static String formatMoney(double amount)` : Formate une valeur numérique (ex: `1250.5` $\rightarrow$ `1 250,50 $`).

---

## 3. Arborescence Complète du Projet

```text
fr.piricraft.core/
├── PiricraftCore.java
├── api/
│   └── PiricraftCoreAPI.java
├── models/
│   ├── PlayerProfile.java
│   └── PiricraftMenu.java
├── managers/
│   ├── DatabaseManager.java
│   ├── EconomyManager.java
│   └── MenuManager.java
├── listeners/
│   ├── PlayerConnectionListener.java
│   └── MenuListener.java
├── commands/
│   ├── MoneyCommand.java
│   ├── PayCommand.java
│   └── EcoAdminCommand.java
└── utils/
    ├── TextUtils.java
    └── NumberUtils.java