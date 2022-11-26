package com.ollethunberg.commands.balance;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.lib.PermissionException;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Balance extends WalletBalanceHelper {

    public void balance(Player player, String target) {
        try {
            if (target == player.getName()) {

                ResultSet balanceResultSet = query(
                        "SELECT p.balance, (select balance from bank_account where player_id=p.uid) as bank_balance from player as p where p.uid = ?",
                        player.getUniqueId().toString());
                if (balanceResultSet.next()) {
                    player.sendMessage(NationsPlusEconomy.walletPrefix + "§eYour wallet balance is: §a"
                            + NationsPlusEconomy.dollarFormat.format(balanceResultSet.getFloat("balance")));
                    player.sendMessage(NationsPlusEconomy.bankPrefix + "§eYour bank balance is: §a"
                            + NationsPlusEconomy.dollarFormat.format(balanceResultSet.getFloat("bank_balance")));
                } else {
                    player.sendMessage(NationsPlusEconomy.walletPrefix + "§cYou don't have a balance yet!");
                }
            } else {
                // first check if the target is a player or nation
                ResultSet playerResultSet = query("SELECT * from player where LOWER(player_name) = LOWER(?)",
                        target);
                if (playerResultSet.next()) {
                    player.sendMessage(NationsPlusEconomy.walletPrefix + "§e" + target + "'s balance is: §a"
                            + NationsPlusEconomy.dollarFormat.format(playerResultSet.getFloat("balance")));
                } else {
                    ResultSet nationResultSet = query("SELECT * from nation where LOWER(name) = LOWER(?)",
                            target);
                    if (nationResultSet.next()) {
                        player.sendMessage(
                                NationsPlusEconomy.bankPrefix + "§6[§r" + target + "§6]§r's balance is: §a"
                                        + NationsPlusEconomy.dollarFormat.format(nationResultSet.getFloat("balance")));
                    } else {
                        player.sendMessage(NationsPlusEconomy.walletPrefix + "§cThat player or nation doesn't exist!");
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

    }

    public void give(Player sender, String target, float amount) throws Exception {
        // check if sender has permission

        if (!sender.hasPermission("npe.give")) {
            throw new PermissionException("");
        }

        // check if the target is a player or nation
        try {
            String entity = getEntityType(target);
            if (entity == "player") {
                // give to player
                // get the player by name from server
                Player targetPlayer = sender.getServer().getPlayer(target);
                if (targetPlayer == null) {
                    throw new Error("§cThat player doesn't exist!");
                }

                float newBalance = addBalancePlayer(targetPlayer.getUniqueId().toString(), amount);
                sender.sendMessage("§4[§cADMIN-TOOL§4] §r§eGave §a" + NationsPlusEconomy.dollarFormat.format(amount)
                        + "§e to §a" + target
                        + "§e. Their new balance is: §a"
                        + NationsPlusEconomy.dollarFormat.format(newBalance));
            } else if (entity == "nation") {
                // give to nation
                float newBalance = addBalanceNation(target, amount);
                sender.sendMessage(
                        "§4[§cADMIN-TOOL§4] §r§eGave §a" + NationsPlusEconomy.dollarFormat.format(amount)
                                + "§e to §6[§r" + target
                                + "§6]§r. Their new balance is: §a"
                                + NationsPlusEconomy.dollarFormat.format(newBalance));

            } else {

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Error e) {
            sender.sendMessage(e.getMessage());
        }

    }
}
