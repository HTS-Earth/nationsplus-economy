package com.ollethunberg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.postgresql.Driver;

import com.ollethunberg.commands.balance.BalanceHandler;
import com.ollethunberg.commands.bank.BankGUI;
import com.ollethunberg.commands.bank.BankHandler;
import com.ollethunberg.commands.bank.BanksHandler;
import com.ollethunberg.commands.bankManager.BankManagerGUI;
import com.ollethunberg.commands.bankManager.BankManagerHandler;
import com.ollethunberg.commands.close.CloseHandler;
import com.ollethunberg.commands.loan.LoanGUI;
import com.ollethunberg.commands.loan.LoanHandler;
import com.ollethunberg.commands.market.MarketGUI;
import com.ollethunberg.commands.market.MarketHandler;
import com.ollethunberg.commands.pay.PayHandler;
import com.ollethunberg.lib.helpers.SQLHelper;
import com.ollethunberg.utils.WalletBalanceHelper;

/*
 * nationsplus-economy java plugin
 */
public class NationsPlusEconomy extends JavaPlugin {
  public static String bankPrefix = "§6[§rBank§6]§r ";
  public static String loanPrefix = "§6[§rBank-Loans§6]§r ";
  public static String walletPrefix = "§6[§rWallet§6]§r ";
  public static String bankManagerPrefix = "§6[§rBank Manager§6]§r ";
  private static Locale usa = new Locale("en", "US");

  public static NumberFormat dollarFormat = NumberFormat.getCurrencyInstance(usa);

  public static final Logger LOGGER = Logger.getLogger("nationsplus-economy");

  public Connection connection;

  /* Command handlers */

  private BalanceHandler balanceHandler;
  private BankHandler bankHandler;
  private BankManagerHandler bankManagerHandler;
  private LoanHandler loanHandler;
  private PayHandler payHandler;
  private BanksHandler banksHandler;
  private CloseHandler closeHandler;
  private BankGUI bankGUI;
  private LoanGUI loanGUI;
  private BankManagerGUI bankManagerGUI;
  public Configuration config;
  private WalletBalanceHelper walletBalanceHelper;
  private MarketHandler marketHandler;
  private MarketGUI marketGUI;

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
      SQLHelper.conn = connection;
      walletBalanceHelper = new WalletBalanceHelper();
      // Register command handlers
      banksHandler = new BanksHandler();
      balanceHandler = new BalanceHandler();
      bankHandler = new BankHandler();
      bankManagerHandler = new BankManagerHandler();
      loanHandler = new LoanHandler();
      payHandler = new PayHandler();
      closeHandler = new CloseHandler();
      bankGUI = new BankGUI();
      loanGUI = new LoanGUI();
      bankManagerGUI = new BankManagerGUI();
      marketHandler = new MarketHandler();
      marketGUI = new MarketGUI();
      // Register commands
      getCommand("balance").setExecutor(balanceHandler);
      getCommand("bank").setExecutor(bankHandler);
      getCommand("bankmanager").setExecutor(bankManagerHandler);
      getCommand("loans").setExecutor(loanHandler);
      getCommand("pay").setExecutor(payHandler);
      getCommand("banks").setExecutor(banksHandler);
      getCommand("close").setExecutor(closeHandler);
      getCommand("market").setExecutor(marketHandler);
      getCommand("sell").setExecutor(marketHandler);
      getCommand("listings").setExecutor(marketHandler);

      /* Register event listeners */
      getServer().getPluginManager().registerEvents(bankGUI, this);
      getServer().getPluginManager().registerEvents(loanGUI, this);
      getServer().getPluginManager().registerEvents(bankManagerGUI, this);
      getServer().getPluginManager().registerEvents(marketGUI, this);

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

        String updateSQL = "update player as p set balance = p.balance + (100 - (select income_tax from nation as n where n.name = p.nation ));";
        try {
          SQLHelper.update(updateSQL);
          // send message to all players
          Bukkit.broadcastMessage(
              NationsPlusEconomy.walletPrefix + "§eYou have been given §a$100§e for being a loyal citizen! ");

          ResultSet playerResultSet = SQLHelper.query(
              "select p.player_name, n.income_tax, p.balance, n.name, p.uid from player as p inner join nation as n on n.name = p.nation where n.income_tax > 0;");

          while (playerResultSet.next()) {

            String playerName = playerResultSet.getString("player_name");
            int tax = playerResultSet.getInt("income_tax");
            if (playerName == null) {
              continue;
            }
            if (playerResultSet.getString("name") != null) {
              SQLHelper.update("update nation as n set balance = n.balance + ? where n.name = ?;", tax,
                  playerResultSet.getString("name"));
            }

            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
              player
                  .sendMessage(NationsPlusEconomy.walletPrefix + "§eYou have paid §c" + tax + "%§e in income-taxes! §c-"
                      + NationsPlusEconomy.dollarFormat.format(tax * 0.01 * 100));
              // your new balance is...
              float balance = playerResultSet.getFloat("balance");
              Bukkit.getPlayer(playerName)
                  .sendMessage(
                      NationsPlusEconomy.walletPrefix + "§eYour new balance is: §a" + dollarFormat.format(balance));
            }

          }
          // banks and loans clock
          try {
            String activeLoans = "select * from bank_loan where active = true AND amount_paid < amount_total;";
            ResultSet activeLoansResult = SQLHelper.query(activeLoans);
            while (activeLoansResult.next()) {
              int loanId = activeLoansResult.getInt("id");
              // int amountPaid = activeLoansResult.getInt("amount_paid");
              int payments_left = activeLoansResult.getInt("payments_left");
              int payments_total = activeLoansResult.getInt("payments_total");
              int amountTotal = activeLoansResult.getInt("amount_total");
              int amountPaid = activeLoansResult.getInt("amount_paid");
              float interest = activeLoansResult.getFloat("interest_rate");
              int amountToPay = amountTotal / payments_total;
              int interestToPay = (int) Math.round((amountTotal - amountPaid) * interest);
              int totalAmountToPay = amountToPay + interestToPay;
              String player_id = activeLoansResult.getString("player_id");
              String bankName = activeLoansResult.getString("bank_name");
              // check if player has enough money
              ResultSet playerBankBalance = SQLHelper.query("select balance from bank_account where player_id= ?;",
                  player_id);
              Player player = Bukkit.getPlayer(UUID.fromString(player_id));
              // tell the player loan information
              if (player != null) {
                player.sendMessage(NationsPlusEconomy.bankPrefix + "§eYou have §a" + (payments_left - 1)
                    + "§e payments left on your loan!");

              }

              if (playerBankBalance.next()) {
                float balance = playerBankBalance.getFloat("balance");
                if (balance >= totalAmountToPay) {
                  // pay the loan
                  SQLHelper.update("update bank_account set balance = balance - ? where player_id = ?;",
                      totalAmountToPay,
                      player_id);
                  payOffLoans(UUID.fromString(player_id), amountToPay, totalAmountToPay, loanId, payments_left,
                      interestToPay, bankName);
                } else {
                  // check if the player can use his wallet balance
                  float walletBalance = walletBalanceHelper.getBalance(player_id, true);
                  if (walletBalance >= totalAmountToPay) {
                    // pay the loan
                    walletBalanceHelper.addBalancePlayer(player_id, -totalAmountToPay);
                    payOffLoans(UUID.fromString(player_id), amountToPay, totalAmountToPay, loanId, payments_left,
                        interestToPay, bankName);
                    continue;
                  }

                  if (player != null) {
                    player.sendMessage(
                        NationsPlusEconomy.bankPrefix
                            + "§c§l[WARNING] §eYou do not have enough money to pay your loan! §c-"
                            + dollarFormat.format(totalAmountToPay)
                            + "§e You need §a"
                            + dollarFormat.format((totalAmountToPay - balance)) + "§e more!");
                  }
                }
              }

            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          // bank account interest
          try {
            String bankAccounts = "select ba.player_id, ba.balance,b.saving_interest, b.bank_name from bank_account as ba inner join bank as b on b.bank_name=ba.bank_name;";
            ResultSet bankAccountsResult = SQLHelper.query(bankAccounts);
            while (bankAccountsResult.next()) {
              String player_id = bankAccountsResult.getString("player_id");
              float balance = bankAccountsResult.getFloat("balance");
              float interest = bankAccountsResult.getFloat("saving_interest");
              float interestToGet = balance * interest;
              String bankName = bankAccountsResult.getString("bank_name");
              Player player = Bukkit.getPlayer(UUID.fromString(player_id));
              if (player != null) {
                player.sendMessage(
                    NationsPlusEconomy.bankPrefix + "§eYou have earned §a" + dollarFormat.format(interestToGet)
                        + "§e in saving interest from §6[§r"
                        + bankName + "§6]§r");
              }
              SQLHelper.update("update bank_account set balance = balance + ? where player_id = ?;", interestToGet,
                  player_id);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          // run again
          runTimer();

        } catch (SQLException e) {

          e.printStackTrace();
        }

      }
    }, 20 * 10 * 10);
  }

  public void onDisable() {
    LOGGER.info("nationsplus-economy disabled");
  }

  public void payOffLoans(UUID playerId, float amountToPay, float totalAmountToPay, int loanId,
      int paymentsLeft, int interestToPay, String bankName) throws SQLException {

    // get the player
    Player player = Bukkit.getPlayer(playerId);
    // log player name
    String playerName = player.getName();
    System.out.println(playerName);

    SQLHelper.update(
        "update bank_loan set amount_paid = amount_paid + ?, payments_left = payments_left - 1 where id = ?;",
        amountToPay, loanId);
    // add the money to the banks balance
    SQLHelper.update("update bank set balance = balance + ? where bank_name = ?;", totalAmountToPay, bankName);
    // check if loan is paid
    if (paymentsLeft - 1 <= 0) {
      SQLHelper.update("update bank_loan set active = false where id = ?;", loanId);

      // send message to player

      if (player != null) {
        player.sendMessage(NationsPlusEconomy.bankPrefix + "§eYour loan has been paid off! ");
      }
    }

    if (player != null) {
      player.sendMessage(
          NationsPlusEconomy.bankPrefix + "§eYou have paid §a" + dollarFormat.format(interestToPay)
              + "§e in interest and §a" + dollarFormat.format(amountToPay)
              + "§e in loan payments! §c-" + dollarFormat.format(totalAmountToPay));
    }

  }

  public void loadConfig() {
    getConfig().options().copyDefaults(true);
    saveConfig();
    config = getConfig();
  }
}
