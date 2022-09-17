package com.ollethunberg.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.ollethunberg.NationsPlusEconomy;

public class SQLHelper {
    private Connection conn;
    Plugin plugin = NationsPlusEconomy.getPlugin(NationsPlusEconomy.class);

    public SQLHelper(Connection connection) {
        conn = connection;
    }

    public Connection getConnection() {
        return conn;
    }

    public static interface QueryCallback {

        public void onQueryDone(ResultSet result) throws SQLException;

    }

    public static interface UpdateCallback {

        public void onQueryDone() throws SQLException;
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public final void queryAsync(final String query, final QueryCallback callback, final Object... args)
            throws SQLException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onQueryDone(query(query, args));
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

    }

    public ResultSet query(String query, Object... args) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        preparedStatement.executeQuery();
        return preparedStatement.getResultSet();
    }

    public void update(String query, Object... args) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        preparedStatement.executeUpdate();
    }

    public final void updateAsync(final String query, final UpdateCallback callback, final Object... args)
            throws SQLException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    update(query, args);
                    callback.onQueryDone();
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

    }
}
