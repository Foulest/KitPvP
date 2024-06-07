/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Synchronized;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.enchants.Enchants;
import net.foulest.kitpvp.kits.Kit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for database operations.
 *
 * @author Foulest
 * @project KitPvP
 */
@SuppressWarnings({"SqlSourceToSinkFlow", "unused"})
public class DatabaseUtil {

    private static HikariDataSource dataSource;

    /**
     * Loads the plugin's databases.
     */
    public static void loadDatabase() {
        // Initializes the DBCP instance.
        DatabaseUtil.initialize(new HikariDataSource());

        if (Settings.usingFlatFile) {
            try {
                // Creates the flat file database if missing.
                if (!Files.exists(Paths.get(Settings.flatFilePath))) {
                    MessageUtil.log(Level.INFO, "Creating flat file database...");
                    Files.createFile(Paths.get(Settings.flatFilePath));
                }
            } catch (IOException ex) {
                MessageUtil.printException(ex);
            }

            // Sets up the DBCP instance for SQLite.
            DatabaseUtil.setupDbcp("jdbc:sqlite:" + Settings.flatFilePath, "org.sqlite.JDBC",
                    null, null, null, false, null);

        } else {
            // Sets up the DBCP instance for MariaDB.
            DatabaseUtil.setupDbcp("jdbc:mariadb://" + Settings.host + ":" + Settings.port + "/" + Settings.database,
                    "org.mariadb.jdbc.Driver", Settings.user, Settings.password,
                    "utf8", true, "SELECT 1;");
        }

        // Creates the PlayerStats table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "PlayerStats",
                "uuid VARCHAR(255) NOT NULL, "
                        + "coins INT, "
                        + "experience INT, "
                        + "kills INT, "
                        + "deaths INT, "
                        + "killstreak INT, "
                        + "topKillstreak INT, "
                        + "usingSoup INT, "
                        + "previousKit VARCHAR(255), "
                        + "PRIMARY KEY (uuid)"
        );

        // Creates the PlayerKits table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "PlayerKits",
                "uuid VARCHAR(255) NOT NULL, "
                        + "kitName VARCHAR(255)"
        );

        // Creates the Bounties table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "Bounties",
                "uuid VARCHAR(255) NOT NULL, "
                        + "bounty INT, "
                        + "benefactor VARCHAR(255), "
                        + "PRIMARY KEY (uuid)"
        );

        // Creates the Enchants table if it doesn't exist.
        DatabaseUtil.createTableIfNotExists(
                "Enchants",
                "uuid VARCHAR(255) NOT NULL, "
                        + "featherFalling INT, "
                        + "thorns INT, "
                        + "protection INT, "
                        + "knockback INT, "
                        + "sharpness INT, "
                        + "punch INT, "
                        + "power INT, "
                        + "PRIMARY KEY (uuid)"
        );
    }

    /**
     * Updates the PlayerStats table in the database.
     */
    public static void updatePlayerStatsTable(@NotNull PlayerData playerData) {
        DatabaseUtil.addDataToTable("PlayerStats", new HashMap<String, Object>() {{
            put("uuid", playerData.getUniqueId().toString());
            put("coins", playerData.getCoins());
            put("experience", playerData.getExperience());
            put("kills", playerData.getKills());
            put("deaths", playerData.getDeaths());
            put("killstreak", playerData.getKillstreak());
            put("topKillstreak", playerData.getTopKillstreak());
            put("usingSoup", (playerData.isUsingSoup() ? 1 : 0));
            put("previousKit", playerData.getPreviousKit().getName());
        }});
    }

    /**
     * Updates the PlayerKits table in the database.
     *
     * @param playerData The player's data.
     */
    public static void updatePlayerKitsTable(@NotNull PlayerData playerData) {
        Set<Kit> ownedKits = playerData.getOwnedKits();
        String uuid = playerData.getUniqueId().toString();

        if (!ownedKits.isEmpty()) {
            DatabaseUtil.deleteDataFromTable("PlayerKits",
                    "uuid = ?", Collections.singletonList(uuid));

            for (Kit kits : ownedKits) {
                if (kits == null) {
                    continue;
                }

                DatabaseUtil.addDataToTable("PlayerKits", new HashMap<String, Object>() {{
                    put("uuid", uuid);
                    put("kitName", kits.getName());
                }});
            }
        }
    }

    /**
     * Updates the Enchants table in the database.
     *
     * @param playerData The player's data.
     */
    public static void updateEnchantsTable(@NotNull PlayerData playerData) {
        Set<Enchants> enchants = playerData.getEnchants();
        String uuid = playerData.getUniqueId().toString();

        // Removes the player from the table if they have no enchants.
        // Otherwise, adds the player to the table.
        if (enchants.isEmpty()) {
            DatabaseUtil.deleteDataFromTable("Enchants",
                    "uuid = ?", Collections.singletonList(uuid));
        } else {
            DatabaseUtil.addDataToTable("Enchants", new HashMap<String, Object>() {{
                put("uuid", uuid);
                put("featherFalling", enchants.contains(Enchants.FEATHER_FALLING) ? 1 : 0);
                put("thorns", enchants.contains(Enchants.THORNS) ? 1 : 0);
                put("protection", enchants.contains(Enchants.PROTECTION) ? 1 : 0);
                put("knockback", enchants.contains(Enchants.KNOCKBACK) ? 1 : 0);
                put("sharpness", enchants.contains(Enchants.SHARPNESS) ? 1 : 0);
                put("punch", enchants.contains(Enchants.PUNCH) ? 1 : 0);
                put("power", enchants.contains(Enchants.POWER) ? 1 : 0);
            }});
        }
    }

    /**
     * Updates the Bounties table in the database.
     *
     * @param playerData The player's data.
     */
    public static void updateBountiesTable(@NotNull PlayerData playerData) {
        int bounty = playerData.getBounty();
        String uuid = playerData.getUniqueId().toString();
        UUID benefactor = playerData.getBenefactor();

        // Removes the player from the table if the bounty is 0 or the benefactor is null.
        // Otherwise, adds the player to the table.
        if (bounty == 0 || benefactor == null) {
            DatabaseUtil.deleteDataFromTable("Bounties",
                    "uuid = ?", Collections.singletonList(uuid));
        } else {
            DatabaseUtil.addDataToTable("Bounties", new HashMap<String, Object>() {{
                put("uuid", uuid);
                put("bounty", bounty);
                put("benefactor", benefactor.toString());
            }});
        }
    }

    /**
     * Initializes the DBCP instance.
     *
     * @param source The BasicDataSource instance.
     */
    @Synchronized
    public static void initialize(HikariDataSource source) {
        if (dataSource == null) {
            dataSource = source;
        }
    }

    /**
     * Sets up the DBCP instance.
     *
     * @param jdbcUrl           The JDBC URL.
     * @param driverClassName   The driver class name.
     * @param user              The database user.
     * @param password          The database password.
     * @param characterEncoding The character encoding.
     * @param useUnicode        Whether to use Unicode.
     * @param validationQuery   The query to validate connections from the pool.
     */
    public static void setupDbcp(String jdbcUrl, String driverClassName, String user, String password,
                                 String characterEncoding, boolean useUnicode, String validationQuery) {
        if (dataSource == null) {
            dataSource = new HikariDataSource();
        }

        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName(driverClassName);

        // SQLite specific adjustments
        if (!jdbcUrl.startsWith("jdbc:sqlite:")) {
            dataSource.setUsername(user);
            dataSource.setPassword(password);
            dataSource.setConnectionTestQuery(validationQuery);
            dataSource.addDataSourceProperty("characterEncoding", characterEncoding);
            dataSource.addDataSourceProperty("useUnicode", Boolean.toString(useUnicode));
        }
    }

    /**
     * Closes the DBCP data source.
     */
    public static void closeDbcp() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Creates a table if it doesn't exist.
     *
     * @param tableName    The table name.
     * @param tableColumns The table column definition.
     */
    public static void createTableIfNotExists(String tableName, String tableColumns) {
        try (Connection connection = (Settings.usingFlatFile ? getSQLiteConnection() : dataSource.getConnection())) {
            DatabaseMetaData metaData = Objects.requireNonNull(connection).getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);

            if (!tables.next()) {
                String createTableSQL = String.format("CREATE TABLE %s (%s)", tableName, tableColumns);

                try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
                    preparedStatement.execute();
                }
            }
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
        }
    }

    /**
     * Deletes a table if it exists.
     *
     * @param tableName The table name.
     * @throws SQLException If a database access error occurs.
     */
    public static void deleteTableIfExists(String tableName) throws SQLException {
        try (Connection connection = (Settings.usingFlatFile ? getSQLiteConnection() : dataSource.getConnection())) {
            DatabaseMetaData metaData = Objects.requireNonNull(connection).getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);

            if (tables.next()) {
                String deleteTableSQL = String.format("DROP TABLE %s", tableName);

                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteTableSQL)) {
                    preparedStatement.execute();
                }
            }
        }
    }

    /**
     * Adds data to a table.
     *
     * @param tableName The table name.
     * @param tableData The data to be added.
     */
    public static void addDataToTable(String tableName, @NotNull HashMap<String, Object> tableData) {
        String columns = String.join(", ", tableData.keySet());
        String placeholders = IntStream.range(0, tableData.size())
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));

        String insertSQL;

        if (Settings.usingFlatFile) {
            // SQLite syntax with INSERT OR REPLACE (assumes table has PRIMARY KEY or UNIQUE constraints)
            insertSQL = String.format("INSERT OR REPLACE INTO %s (%s) VALUES (%s)",
                    tableName, columns, placeholders);
        } else {
            // MySQL/MariaDB syntax with ON DUPLICATE KEY UPDATE
            String updateStatement = " ON DUPLICATE KEY UPDATE " + tableData.keySet().stream()
                    .map(column -> String.format("%s = VALUES(%s)", column, column))
                    .collect(Collectors.joining(", "));

            insertSQL = String.format("INSERT INTO %s (%s) VALUES (%s)%s",
                    tableName, columns, placeholders, updateStatement);
        }

        try (Connection connection = (Settings.usingFlatFile ? getSQLiteConnection() : dataSource.getConnection())) {
            try (PreparedStatement preparedStatement = Objects.requireNonNull(connection).prepareStatement(insertSQL)) {
                int index = 1;
                for (Object value : tableData.values()) {
                    preparedStatement.setObject(index++, value);
                }

                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
        }
    }

    /**
     * Inserts values into a table if the column is empty.
     *
     * @param tableName The table name.
     * @param tableData The data to be added.
     */
    public static void addDefaultDataToTable(String tableName, @NotNull HashMap<String, Object> tableData) {
        // Check if the table is empty
        String checkTableEmptySQL = String.format("SELECT COUNT(*) FROM %s", tableName);

        try (Connection connection = (Settings.usingFlatFile ? getSQLiteConnection() : dataSource.getConnection());
             PreparedStatement checkTableEmptyStmt = Objects.requireNonNull(connection).prepareStatement(checkTableEmptySQL)) {

            ResultSet rs = checkTableEmptyStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Table is not empty, no need to insert default data
            }

            // Proceed with insert since table is empty
            String columns = String.join(", ", tableData.keySet());
            String placeholders = String.join(", ", Collections.nCopies(tableData.size(), "?"));
            String insertSQL = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

            try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                int index = 1;
                for (Object value : tableData.values()) {
                    insertStmt.setObject(index++, value);
                }
                insertStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
        }
    }

    /**
     * Loads data from a table.
     *
     * @param tableName  The table name.
     * @param condition  The condition for the SQL query.
     * @param parameters The parameters to replace placeholders in the condition.
     * @return A list of HashMaps representing the rows fetched.
     * @throws SQLException If a database access error occurs.
     */
    public static @NotNull List<HashMap<String, Object>> loadDataFromTable(String tableName,
                                                                           String condition,
                                                                           @NotNull List<Object> parameters) throws SQLException {
        String selectSQL = String.format("SELECT * FROM %s WHERE %s", tableName, condition);

        try (Connection connection = (Settings.usingFlatFile ? getSQLiteConnection() : dataSource.getConnection())) {
            try (PreparedStatement preparedStatement = Objects.requireNonNull(connection).prepareStatement(selectSQL)) {
                for (int i = 0; i < parameters.size(); i++) {
                    preparedStatement.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<HashMap<String, Object>> rows = new ArrayList<>();

                    while (resultSet.next()) {
                        HashMap<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            row.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                        }
                        rows.add(row);
                    }
                    return rows;
                }
            }
        }
    }

    /**
     * Deletes data from a table.
     *
     * @param tableName  The table name.
     * @param condition  The condition for the SQL query.
     * @param parameters The parameters to replace placeholders in the condition.
     */
    public static void deleteDataFromTable(String tableName,
                                           String condition,
                                           @NotNull List<Object> parameters) {
        String deleteSQL = String.format("DELETE FROM %s WHERE %s", tableName, condition);

        try (Connection connection = (Settings.usingFlatFile ? getSQLiteConnection() : dataSource.getConnection());
             PreparedStatement preparedStatement = Objects.requireNonNull(connection).prepareStatement(deleteSQL)) {
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
        }
    }

    /**
     * Method to get a SQLite connection directly without DBCP.
     *
     * @return The SQLite connection.
     * @throws SQLException If a database access error occurs.
     */
    public static @Nullable Connection getSQLiteConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + Settings.flatFilePath);
        } catch (ClassNotFoundException ex) {
            MessageUtil.printException(ex);
            return null;
        }
    }
}
