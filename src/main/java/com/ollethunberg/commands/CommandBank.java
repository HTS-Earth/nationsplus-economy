package com.ollethunberg.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.utils.BalanceHelper;

public class CommandBank extends BalanceHelper {
    public CommandBank(Connection _connection) {
        super(_connection);
    }

    // Create bank command
    // the cost of creating a bank is $1000
    // the bank is created with a balance of $0
    public void createBank(Player player, String bankName) throws SQLException {
        // check if the player has a big enough balance to create a bank
        ResultSet balanceResultSet = query("SELECT balance from player as p inner join  where uid = ?",
                player.getUniqueId().toString());
        if (balanceResultSet.next()) {
            if (balanceResultSet.getFloat("balance") >= 1000) {
                // check if the bank already exists
                ResultSet bankResultSet = query("SELECT * from bank where LOWER(name) = LOWER(?)", bankName);
                if (bankResultSet.next()) {
                    player.sendMessage("§cThat bank already exists!");
                } else {
                    // create the bank
                    String createBankSQL = "INSERT INTO bank (bank_name, balance, owner, saving_interest) VALUES (?, ?, ?, ? )";

                    update(createBankSQL, bankName, 0, player.getUniqueId().toString(), 0.05);

                    // subtract the cost of creating the bank from the player's balance
                    addBalancePlayer(player.getUniqueId().toString(), -1000);

                    player.sendMessage("§eYou created a bank called §6[§r" + bankName + "§6]§r!");
                }
            } else {
                player.sendMessage("§cYou don't have enough money to create a bank!");
            }
        } else {
            player.sendMessage("§cYou don't have a balance yet!");
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
                player.sendMessage("§eYou set the interest to §a" + interestFloat * 100 + "%§e!");
            } else {
                player.sendMessage("§cYou don't own a bank!");
            }
        } else {
            player.sendMessage("§cThe interest must be between 0 and 1!");
        }

    }
}
