package net.foulest.kitpvp.util;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Foulest
 * @project KitPvP
 */
public class DatabaseUtil {

    private static HikariDataSource hikari;

    /**
     * Initializes the Hikari instance.
     *
     * @param source The Hikari data source.
     */
    public static synchronized void initialize(HikariDataSource source) {
        if (hikari == null) {
            hikari = source;
        }
    }

    /**
     * Closes the Hikari instance.
     */
    public static void closeHikari() {
        if (hikari != null) {
            hikari.close();
        }
    }

    /**
     * Sets up the Hikari instance.
     *
     * @param poolName            The pool name.
     * @param jdbcUrl             The JDBC URL.
     * @param driverClassName     The driver class name.
     * @param user                The database user.
     * @param password            The database password.
     * @param characterEncoding   The character encoding.
     * @param useUnicode          The use unicode.
     * @param connectionTestQuery The connection test query.
     */
    public static void setupHikari(String poolName, String jdbcUrl, String driverClassName,
                                   String user, String password, String characterEncoding, String useUnicode,
                                   String connectionTestQuery) {
        hikari.setPoolName(poolName);
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setDriverClassName(driverClassName);
        hikari.addDataSourceProperty("user", user);
        hikari.addDataSourceProperty("password", password);
        hikari.addDataSourceProperty("characterEncoding", characterEncoding);
        hikari.addDataSourceProperty("useUnicode", useUnicode);
        hikari.setConnectionTestQuery(connectionTestQuery);
    }

    /**
     * Creates a table if it doesn't exist.
     *
     * @param tableName    The table name.
     * @param tableColumns The table column definition.
     */
    public static void createTableIfNotExists(String tableName, String tableColumns) {
        try (Connection connection = hikari.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);

            if (!tables.next()) {
                String createTableSQL = String.format("CREATE TABLE %s (%s)", tableName, tableColumns);

                try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
                    preparedStatement.execute();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Deletes a table if it exists.
     *
     * @param tableName The table name.
     * @throws SQLException If a database access error occurs.
     */
    public static void deleteTableIfExists(String tableName) throws SQLException {
        try (Connection connection = hikari.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
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
    public static void addDataToTable(String tableName, HashMap<String, Object> tableData) {
        String columns = String.join(", ", tableData.keySet());
        String placeholders = IntStream.range(0, tableData.size())
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));

        String updateStatement = " ON DUPLICATE KEY UPDATE " + tableData.keySet().stream()
                        .map(column -> String.format("%s = VALUES(%s)", column, column))
                        .collect(Collectors.joining(", "));

        String insertSQL = String.format("INSERT INTO %s (%s) VALUES (%s)%s", tableName, columns, placeholders, updateStatement);

        try (Connection connection = hikari.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                int index = 1;
                for (Object value : tableData.values()) {
                    preparedStatement.setObject(index++, value);
                }

                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Inserts values into a table if the column is empty.
     *
     * @param tableName The table name.
     * @param tableData The data to be added.
     */
    public static void addDefaultDataToTable(String tableName, HashMap<String, Object> tableData) {
        String columns = String.join(", ", tableData.keySet());
        String placeholders = IntStream.range(0, tableData.size())
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));

        String checkIfExistsSQL = String.format("SELECT 1 FROM %s LIMIT 1", tableName);
        String insertSQL = String.format("INSERT INTO %s (%s) SELECT %s FROM DUAL WHERE NOT EXISTS (%s)",
                tableName, columns, placeholders, checkIfExistsSQL);

        try (Connection connection = hikari.getConnection()) {
            try (PreparedStatement checkIfExistsStatement = connection.prepareStatement(checkIfExistsSQL);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

                // Check if any row exists in the table
                if (checkIfExistsStatement.executeQuery().next()) {
                    return; // Data already exists, no need to insert
                }

                // Bind values and execute the insert statement
                int index = 1;
                for (Object value : tableData.values()) {
                    insertStatement.setObject(index++, value);
                }
                insertStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load data from a table.
     *
     * @param tableName  The table name.
     * @param condition  The condition for the SQL query.
     * @param parameters The parameters to replace placeholders in the condition.
     * @return A list of HashMaps representing the rows fetched.
     * @throws SQLException If a database access error occurs.
     */
    public static List<HashMap<String, Object>> loadDataFromTable(String tableName, String condition, List<Object> parameters) throws SQLException {
        String selectSQL = String.format("SELECT * FROM %s WHERE %s", tableName, condition);

        try (Connection connection = hikari.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
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
     * Delete data from a table.
     *
     * @param tableName The table name.
     * @param condition The condition for the SQL query.
     * @param parameters The parameters to replace placeholders in the condition.
     */
    public static void deleteDataFromTable(String tableName, String condition, List<Object> parameters) {
        String deleteSQL = String.format("DELETE FROM %s WHERE %s", tableName, condition);

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
