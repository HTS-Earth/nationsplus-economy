package com.ollethunberg.commands.bank;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.lib.ColorHelper;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Bank extends WalletBalanceHelper {
    BankGUI bankGUI;

    public Bank() {
        bankGUI = new BankGUI();
    }

    // Create bank command
    // the cost of creating a bank is $1000
    // the bank is created with a balance of $0
    public void createBank(Player player, String bankName) throws SQLException {
        // check if the player has a big enough balance to create a bank
        ResultSet balanceResultSet = query("SELECT balance from player where uid = ?",
                player.getUniqueId().toString());
        if (balanceResultSet.next()) {
            if (balanceResultSet.getFloat("balance") >= 1000) {
                // check if the bank already exists
                ResultSet bankResultSet = query("SELECT * from bank where LOWER(bank_name) = LOWER(?)", bankName);
                if (bankResultSet.next()) {
                    player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cThat bank already exists!");
                } else {
                    // create the bank
                    String createBankSQL = "INSERT INTO bank (bank_name, balance, owner, saving_interest) VALUES (?, ?, ?, ? )";

                    update(createBankSQL, bankName, 0, player.getUniqueId().toString(), 0.05);

                    // subtract the cost of creating the bank from the player's balance
                    addBalancePlayer(player.getUniqueId().toString(), -1000);

                    player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§eYou created a bank called §6[§r"
                            + bankName + "§6]§r!");
                }
            } else {
                player.sendMessage(
                        NationsPlusEconomy.bankManagerPrefix
                                + "§cYou don't have enough money to create a bank! ($1000)");
            }
        } else {
            player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cYou don't have a balance yet!");
        }
    }

    public void setInterest(Player player, String interest) throws SQLException {
        // convert interest to float
        float interestFloat = Float.parseFloat(interest) / 100;
        // check if the interest is between 0 and 1
        if (interestFloat >= 0 && interestFloat <= 1) {
            // check if the player is the owner of the bank
            ResultSet bankResultSet = query("SELECT * from bank where LOWER(owner) = LOWER(?)",
                    player.getUniqueId().toString());
            if (bankResultSet.next()) {
                // set the interest
                String setInterestSQL = "UPDATE bank SET saving_interest = ? WHERE LOWER(owner) = LOWER(?)";
                update(setInterestSQL, interestFloat, player.getUniqueId().toString());
                player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§eYou set the interest to §a"
                        + interestFloat * 100 + "%§e!");
            } else {
                player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cYou don't own a bank!");
            }
        } else {
            player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cThe interest must be between 0 and 1!");
        }

    }

    public void listBanks(Player player) throws SQLException {
        // check if the player is the owner of the bank

        // list the banks
        ResultSet banksResultSet = query(
                "SELECT b.*, p.player_name,(SELECT SUM(balance) from bank_account where bank_name=b.bank_name) as customers_balance, (select count(*) from bank_account where bank_name=b.bank_name) as customers from bank as b inner join player as p on b.owner = p.uid");
        player.sendMessage("§eBanks:");
        while (banksResultSet.next()) {
            player.sendMessage(
                    "-§l§6[§r" + banksResultSet.getString("bank_name") + "§6]§r-");

            player.sendMessage("§eAvailable funds: §a"
                    + NationsPlusEconomy.dollarFormat.format(banksResultSet.getFloat("balance")));
            player.sendMessage("§eCustomer's funds: §a"
                    + NationsPlusEconomy.dollarFormat.format(banksResultSet.getFloat("customers_balance")));
            player.sendMessage("§eSaving interest: §a"
                    + banksResultSet.getFloat("saving_interest") * 100 + "%§r");
            // safety rating is the ratio between the bank's balance and the total balance
            // of all customers
            Float safetyRating = banksResultSet.getFloat("balance")
                    / banksResultSet.getFloat("customers_balance");
            player.sendMessage("§eSafety-rating§r: " + ColorHelper.addColorToPercentage(safetyRating));

            player.sendMessage("§eOwner: §6" + banksResultSet.getString("player_name"));
            player.sendMessage("§eCustomers: §a" + banksResultSet.getInt("customers"));

        }

    }

    public void bankAccount(Player player) throws SQLException {
        bankGUI.bankAccount(player);
    }

    public void openBankAccount(Player player, String bankName) throws SQLException {
        // check if the bank exists
        // check if the player is the owner of the bank
        // check if the player already has an account in the bank
        // create the account

        ResultSet bankResultSet = query("SELECT * from bank where LOWER(bank_name) = LOWER(?)", bankName);
        if (bankResultSet.next()) {

            // check if the player already has an account in the bank
            ResultSet bankAccountResultSet = query("SELECT * from bank_account where LOWER(player_id) = LOWER(?)",
                    player.getUniqueId().toString());
            if (bankAccountResultSet.next()) {
                player.sendMessage(NationsPlusEconomy.bankPrefix + "§cYou already have a bank account in the bank §6[§r"
                        + bankName + "§6]§r!");
            } else {
                // create the account
                String createBankAccountSQL = "INSERT INTO bank_account (bank_name, player_id, balance) VALUES (?, ?, ? )";
                update(createBankAccountSQL, bankName, player.getUniqueId().toString(), 0);
                player.sendMessage("§eYou created an account in §6[§r" + bankName + "§6]§r!");
                player.sendMessage(
                        NationsPlusEconomy.bankPrefix + "§eYou can now deposit and withdraw money from the bank!");
            }
        } else {
            player.sendMessage(NationsPlusEconomy.bankPrefix + "§cThat bank doesn't exist!");
        }

    }

    public void closeBankAccount(Player player) throws SQLException {
        // check if the player has an account in the bank
        // delete the account
        ResultSet bankAccountResultSet = query("SELECT * from bank_account where LOWER(player_id) = LOWER(?)",
                player.getUniqueId().toString());
        if (bankAccountResultSet.next()) {
            // withdraw all the money
            withdraw(player, bankAccountResultSet.getFloat("balance"));
            // delete the account
            String deleteBankAccountSQL = "DELETE FROM bank_account WHERE LOWER(player_id) = LOWER(?)";
            update(deleteBankAccountSQL, player.getUniqueId().toString());
            player.sendMessage(NationsPlusEconomy.bankPrefix + "§eYou closed your bank account!");
        } else {
            player.sendMessage(NationsPlusEconomy.bankPrefix + "§cYou don't have a bank account!");
        }
    }

    // deposit money into the bank
    public void deposit(Player player, Float amount) throws SQLException, Error {
        // Check if the amount is positive
        if (amount <= 0) {
            throw new Error("You can't deposit a negative amount!");
        }
        // check if the player has an account in the bank
        // check if the player has enough money
        // add the money to the bank
        // subtract the money from the player
        ResultSet bankAccountResultSet = query("SELECT * from bank_account where LOWER(player_id) = LOWER(?)",
                player.getUniqueId().toString());
        if (bankAccountResultSet.next()) {
            // check if the player has enough money
            ResultSet balanceResultSet = query("SELECT balance from player where uid = ?",
                    player.getUniqueId().toString());
            if (balanceResultSet.next()) {
                if (balanceResultSet.getFloat("balance") >= amount) {
                    // add the money to the bank
                    String depositSQL = "UPDATE bank_account SET balance = balance + ? WHERE LOWER(player_id) = LOWER(?)";
                    String updateBankBalanceSQL = "UPDATE bank SET balance = balance + ? WHERE LOWER(bank_name) = LOWER(?)";
                    update(updateBankBalanceSQL, amount, bankAccountResultSet.getString("bank_name"));
                    update(depositSQL, amount, player.getUniqueId().toString());
                    // subtract the money from the player
                    addBalancePlayer(player.getUniqueId().toString(), -amount);
                    player.sendMessage(
                            NationsPlusEconomy.bankPrefix + "§eYou deposited §a"
                                    + NationsPlusEconomy.dollarFormat.format(amount) + "§e into the bank!");
                } else {
                    throw new Error(NationsPlusEconomy.bankPrefix + "You don't have enough money! You need §a"
                            + NationsPlusEconomy.dollarFormat.format(amount - balanceResultSet.getFloat("balance"))
                            + "§r more!");
                }
            } else {
                throw new Error("You don't exist in the database. Something went terribly wrong!");
            }
        } else {
            throw new Error(NationsPlusEconomy.bankPrefix + "§cYou don't have a bank account!");

        }
    }

    public void withdraw(Player player, Float amount) throws SQLException {
        if (amount <= 0) {
            player.sendMessage(NationsPlusEconomy.bankPrefix + "§cYou can't withdraw a negative amount!");
            return;
        }
        // withdraw the money from the bank using the banks balance. If the bank doesn't
        // have enough money, the player will get a message
        // add the money to the player
        ResultSet bankAccountResultSet = query("SELECT * from bank_account where LOWER(player_id) = LOWER(?)",
                player.getUniqueId().toString());
        if (bankAccountResultSet.next()) {
            // check if the bank has enough money
            ResultSet bankResultSet = query("SELECT * from bank where LOWER(bank_name) = LOWER(?)",
                    bankAccountResultSet.getString("bank_name"));
            if (bankResultSet.next()) {
                if (bankResultSet.getFloat("balance") >= amount) {
                    // withdraw the money from the bank account
                    String withdrawSQL = "UPDATE bank_account SET balance = balance - ? WHERE LOWER(player_id) = LOWER(?)";
                    update(withdrawSQL, amount, player.getUniqueId().toString());
                    // remove the money from the banks balance
                    String removeMoneyFromBankSQL = "UPDATE bank SET balance = balance - ? WHERE LOWER(bank_name) = LOWER(?)";
                    update(removeMoneyFromBankSQL, amount, bankAccountResultSet.getString("bank_name"));
                    // add the money to the player
                    addBalancePlayer(player.getUniqueId().toString(), amount);
                    player.sendMessage(
                            NationsPlusEconomy.bankPrefix + "§eYou withdrew §a"
                                    + NationsPlusEconomy.dollarFormat.format(amount) + "§e from the bank!");
                } else {
                    player.sendMessage(NationsPlusEconomy.bankPrefix + "§cThe bank doesn't have enough money!");
                }
            } else {
                player.sendMessage(NationsPlusEconomy.bankPrefix + "§cThe bank doesn't exist!");
            }
        } else {
            player.sendMessage(NationsPlusEconomy.bankPrefix + "§cYou don't have a bank account!");
        }

    }
}
