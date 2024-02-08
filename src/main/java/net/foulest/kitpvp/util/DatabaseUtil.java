package net.foulest.kitpvp.util;

import lombok.Synchronized;
import org.apache.commons.dbcp2.BasicDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for database operations.
 *
 * @author Foulest
 * @project KitPvP
 */
@SuppressWarnings("SqlSourceToSinkFlow")
public class DatabaseUtil {

    private static BasicDataSource dataSource;

    /**
     * Initializes the DBCP instance.
     *
     * @param source The BasicDataSource instance.
     */
    @Synchronized
    public static void initialize(BasicDataSource source) {
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
            dataSource = new BasicDataSource();
        }

        dataSource.setUrl(jdbcUrl);
        dataSource.setDriverClassName(driverClassName);

        // SQLite specific adjustments
        if (!jdbcUrl.startsWith("jdbc:sqlite:")) {
            dataSource.setUsername(user);
            dataSource.setPassword(password);
            dataSource.setValidationQuery(validationQuery);
            dataSource.addConnectionProperty("characterEncoding", characterEncoding);
            dataSource.addConnectionProperty("useUnicode", Boolean.toString(useUnicode));
        }
    }

    /**
     * Closes the DBCP data source.
     */
    public static void closeDbcp() {
        try {
            if (dataSource != null) {
                dataSource.close();
            }
        } catch (SQLException ex) {
            MessageUtil.printException(ex);
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
