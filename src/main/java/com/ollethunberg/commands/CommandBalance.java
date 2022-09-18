package com.ollethunberg.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.utils.BalanceHelper;

public class CommandBalance extends BalanceHelper {

    public CommandBalance(Connection conn) {
        super(conn);
    }

    public void balance(Player player, String target) {
        try {
            if (target == player.getName()) {

                ResultSet balanceResultSet = query("SELECT balance from player where uid = ?",
                        player.getUniqueId().toString());
                if (balanceResultSet.next()) {
                    player.sendMessage("§eYour balance is: §a$" + balanceResultSet.getFloat("balance"));
                } else {
                    player.sendMessage("§cYou don't have a balance yet!");
                }
            } else {
                // first check if the target is a player or nation
                ResultSet playerResultSet = query("SELECT * from player where LOWER(player_name) = LOWER(?)",
                        target);
                if (playerResultSet.next()) {
                    player.sendMessage("§e" + target + "'s balance is: §a$" + playerResultSet.getFloat("balance"));
                } else {
                    ResultSet nationResultSet = query("SELECT * from nation where LOWER(name) = LOWER(?)",
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

            e.printStackTrace();
        }

    }

    public void give(Player sender, String target, float amount) {
        // check if the target is a player or nation
        try {
            String entity = getEntityType(target);
            if (entity == "player") {
                // give to player
                // get the player by name from server
                Player targetPlayer = sender.getServer().getPlayer(target);

                float newBalance = addBalancePlayer(targetPlayer.getUniqueId().toString(), amount);
                sender.sendMessage("§4[§cADMIN-TOOL§4] §r§eGave §a$" + amount + "§e to §a" + target
                        + "§e. Their new balance is: §a$"
                        + newBalance);
            } else if (entity == "nation") {
                // give to nation
                float newBalance = addBalanceNation(target, amount);
                sender.sendMessage(
                        "§4[§cADMIN-TOOL§4] §r§eGave §a$" + amount + "§e to §6[§r" + target
                                + "§6]§r. Their new balance is: §a$"
                                + newBalance);

            } else {

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Error e) {
            sender.sendMessage(e.getMessage());
        }

    }
}
