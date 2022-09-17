package com.ollethunberg;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.Configuration;
import org.postgresql.Driver;

import java.sql.*;

/*
 * nationsplus-economy java plugin
 */
public class NationsPlusEconomy extends JavaPlugin {
  private static final Logger LOGGER = Logger.getLogger("nationsplus-economy");
  public Connection connection;
  private CommandHandler commandHandler;
  public Configuration config;

  public void onEnable() {
    loadConfig();

    LOGGER.info("nationsplus-economy enabled");
    try {
      DriverManager.registerDriver(new Driver());
      connection = DriverManager.getConnection(
          "jdbc:postgresql://" + config.getString("database.ip") + ":" + config.getInt("database.port") + "/"
              + config.getString("database.database") + "?stringtype=unspecified",
          config.getString("database.username"), config.getString("database.password"));

      getLogger().info(connection.toString() + " connected to DB successfully!");

      // Register command handler
      commandHandler = new CommandHandler(connection);
      // Register events listeners that needs a SQL connection

      // Register commands
      getCommand("balance").setExecutor(commandHandler);

    } catch (SQLException e) {
      System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onDisable() {
    LOGGER.info("nationsplus-economy disabled");
  }

  public void loadConfig() {
    getConfig().options().copyDefaults(true);
    saveConfig();
    config = getConfig();

  }
}
