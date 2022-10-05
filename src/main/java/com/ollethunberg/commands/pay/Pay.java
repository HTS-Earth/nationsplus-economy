package com.ollethunberg.commands.pay;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Pay extends WalletBalanceHelper {
    public Pay(Connection conn) {
        super(conn);
    }

    // pay command
    public void pay(Player sender, String target, float amount) {
        if (amount <= 0) {
            sender.sendMessage(NationsPlusEconomy.walletPrefix + "§cYou can't pay a negative amount!");
            return;
        }
        // check which entity type the target is
        try {

            // check if the sender has enough money
            ResultSet senderBalance = query("SELECT balance from player where uid = ?",
                    sender.getUniqueId().toString());
            if (senderBalance.next()) {
                if (senderBalance.getFloat("balance") >= amount) {
                    // check if the target exists
                    ResultSet targetPlayer = query("SELECT * from player where LOWER(player_name) = LOWER(?)",
                            target);
                    if (targetPlayer.next()) {
                        // check if the target is the sender
                        if (targetPlayer.getString("uid").equalsIgnoreCase(sender.getUniqueId().toString())) {
                            sender.sendMessage(NationsPlusEconomy.walletPrefix + "§cYou can't pay yourself!");
                        } else {
                            // pay the target
                            update("UPDATE player SET balance = balance + ? where LOWER(player_name) = LOWER(?)",
                                    amount, target);
                            update("UPDATE player SET balance = balance - ? where uid = ?", amount,
                                    sender.getUniqueId().toString());

                            sender.sendMessage(NationsPlusEconomy.walletPrefix + "§eYou paid §6[§r" + target
                                    + "§6]§r §a$" + amount);
                            // get the target player from server
                            Player targetPlayerFromServer = sender.getServer().getPlayer(target);
                            if (targetPlayerFromServer != null) {
                                targetPlayerFromServer.sendMessage(NationsPlusEconomy.walletPrefix +
                                        "§eYou received §a" + NationsPlusEconomy.dollarFormat.format(amount)
                                        + " §efrom §6[§r" + sender.getName() + "§6]§r");
                            }

                        }
                    } else {
                        sender.sendMessage(NationsPlusEconomy.walletPrefix + "§cThat player doesn't exist!");
                    }

                } else {
                    sender.sendMessage(NationsPlusEconomy.walletPrefix + "§cYou don't have enough money!");
                }
            } else {
                sender.sendMessage(NationsPlusEconomy.walletPrefix + "§cYou don't have a balance yet!");
            }
        } catch (SQLException e) {

            e.printStackTrace();
        } catch (Error e) {
            sender.sendMessage(e.toString());
            e.printStackTrace();
        }

    }

}
