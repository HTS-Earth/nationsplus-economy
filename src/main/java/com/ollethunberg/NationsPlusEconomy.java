package com.ollethunberg;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.postgresql.Driver;

import com.ollethunberg.lib.SQLHelper;

import java.sql.*;

/*
 * nationsplus-economy java plugin
 */
public class NationsPlusEconomy extends JavaPlugin {
  private static final Logger LOGGER = Logger.getLogger("nationsplus-economy");
  private SQLHelper sqlHelper;
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
      sqlHelper = new SQLHelper(connection);
      // Register command handler
      commandHandler = new CommandHandler(connection);
      // Register events listeners that needs a SQL connection

      // Register commands
      getCommand("balance").setExecutor(commandHandler);
      this.runTimer();
    } catch (SQLException e) {
      System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
    } catch (Exception e) {

      e.printStackTrace();
    }

  }

  public void runTimer() {
    Bukkit.getScheduler().runTaskLater(this, new Runnable() {
      @Override
      public void run() {
        // this runsevery hour
        // give every player $100 every hour

        String updateSQL = "update player as p set balance = p.balance + (100 - (select tax from nation as n where n.name = p.nation ));";
        try {
          sqlHelper.update(updateSQL);
          // send message to all players
          Bukkit.broadcastMessage("§eYou have been given $100 for being a loyal citizen! §a$+100");
          // inform each player about how much of the tax they have paid
          ResultSet playerResultSet = sqlHelper.query(
              "select p.player_name, n.tax, p.balance, n.name from player as p inner join nation as n on n.name = p.nation where n.tax > 0;");

          while (playerResultSet.next()) {
            String playerName = playerResultSet.getString("player_name");
            int tax = playerResultSet.getInt("tax");
            if (playerName != null && tax > 0) {
              Bukkit.getPlayer(playerName).sendMessage("§eYou have paid §a" + tax + "§r%§e in taxes!");
              // your new balance is...
              float balance = playerResultSet.getFloat("balance");
              Bukkit.getPlayer(playerName).sendMessage("§eYour new balance is: §a$" + balance);
              // give the nation the money
              if (playerResultSet.getString("name") != null) {
                sqlHelper.update("update nation as n set balance = n.balance + ? where n.name = ?;", tax,
                    playerResultSet.getString("name"));

              }
            }
          }
          // run again
          runTimer();

        } catch (SQLException e) {

          e.printStackTrace();
        }

      }
    }, 20 * 60 * 60);
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
