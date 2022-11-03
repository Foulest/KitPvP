package net.foulest.kitpvp.util;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

/**
 * @author Foulest
 * @project KitPvP
 */
public class DatabaseUtil {

    public static HikariDataSource hikari;

    public static boolean exists(String scope, String table, String column, String logicGate, String data) {
        if (data != null) {
            data = "'" + data + "'";
        }

        try (Connection connection = hikari.getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT " + scope + " FROM "
                     + table + " WHERE " + column + logicGate + data)) {
            ResultSet result = select.executeQuery();

            if (result.next()) {
                return true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static void update(String command) {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(command);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static ResultSet query(String scope, String table, String column, String logicGate, String data) {
        if (data != null) {
            data = "'" + data + "'";
        }

        ResultSet result = null;

        try (Connection connection = hikari.getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT " + scope + " FROM "
                     + table + " WHERE " + column + logicGate + data)) {
            result = select.executeQuery();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static Object get(String selected, String scope, String table, String column, String logicGate, String data) {
        if (data != null) {
            data = "'" + data + "'";
        }

        ResultSet result;

        try (Connection connection = hikari.getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT " + scope + " FROM "
                     + table + " WHERE " + column + logicGate + data)) {
            result = select.executeQuery();

            if (result != null && result.next()) {
                return result.getObject(selected);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
