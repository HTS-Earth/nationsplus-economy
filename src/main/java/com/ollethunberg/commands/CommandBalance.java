package com.ollethunberg.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.lib.SQLHelper;

public class CommandBalance {
    SQLHelper sqlHelper;

    public CommandBalance(Connection _connection) {
        sqlHelper = new SQLHelper(_connection);
    }

    public void execute(Player player, String target) {
        try {
            // log target
            System.out.println("target: " + target);

            if (target == player.getName()) {

                ResultSet balanceResultSet = sqlHelper.query("SELECT balance from player where uid = ?",
                        player.getUniqueId().toString());
                if (balanceResultSet.next()) {
                    player.sendMessage("§eYour balance is: §a$" + balanceResultSet.getFloat("balance"));
                } else {
                    player.sendMessage("§cYou don't have a balance yet!");
                }
            } else {
                // first check if the target is a player or nation
                ResultSet playerResultSet = sqlHelper.query("SELECT * from player where LOWER(player_name) = LOWER(?)",
                        target);
                if (playerResultSet.next()) {
                    player.sendMessage("§e" + target + "'s balance is: §a$" + playerResultSet.getFloat("balance"));
                } else {
                    ResultSet nationResultSet = sqlHelper.query("SELECT * from nation where LOWER(name) = LOWER(?)",
                            target);
                    if (nationResultSet.next()) {
                        player.sendMessage(
                                "§6[§r" + target + "§6]§r's balance is: §a$" + nationResultSet.getFloat("balance"));
                    } else {
                        player.sendMessage("§cThat player or nation doesn't exist!");
                    }
                }
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
