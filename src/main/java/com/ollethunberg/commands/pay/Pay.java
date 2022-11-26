package com.ollethunberg.commands.pay;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.lib.models.Nation;
import com.ollethunberg.lib.models.db.DBPlayer;
import com.ollethunberg.lib.helpers.NationHelper;
import com.ollethunberg.lib.helpers.PlayerHelper;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Pay extends WalletBalanceHelper {

    PlayerHelper playerHelper = new PlayerHelper();
    NationHelper nationHelper = new NationHelper();

    // pay command
    public void pay(Player sender, String target, float amount) throws SQLException, Exception {
        if (amount <= 0) {
            sender.sendMessage(NationsPlusEconomy.walletPrefix + "§cYou can't pay a negative amount!");
            return;
        }
        // check which entity type the target is

        // check if the sender has enough money
        ResultSet senderBalance = query("SELECT balance from player where uid = ?",
                sender.getUniqueId().toString());

        if (!senderBalance.next())
            throw new Error("You don't have a wallet!");

        if (senderBalance.getFloat("balance") < amount)
            throw new Error("§cYou don't have enough money!");

        // check if the target exists

        DBPlayer targetPlayer = playerHelper.getPlayerByName(target);
        if (targetPlayer == null) {
            throw new Error("§cThat player doesn't exist!");
        }

        // check if the target is the sender
        if (targetPlayer.uid.equalsIgnoreCase(sender.getUniqueId().toString())) {
            sender.sendMessage(NationsPlusEconomy.walletPrefix + "§cYou can't pay yourself!");
        } else {
            // get the nation of the receiver
            Nation nation = nationHelper.getNation(targetPlayer.nation);
            // get the transfer tax rate of the nation
            float tax = nation != null ? ((nation.transfer_tax / 100f) * amount) : 0;

            // get the amount that the receiver will receive
            float receiverAmount = amount - tax;

            // pay the target
            update("UPDATE player SET balance = balance + ? where LOWER(player_name) = LOWER(?)",
                    receiverAmount, target);
            update("UPDATE player SET balance = balance - ? where uid = ?", amount,
                    sender.getUniqueId().toString());

            if (nation != null) {
                addBalanceNation(nation.name, tax);
            }

            sender.sendMessage(NationsPlusEconomy.walletPrefix + "§eYou paid §6[§r" + target
                    + "§6]§r §a$" + amount + ", §eand the nation took §c$" + tax + "§e as tax!");
            // get the target player from server
            Player targetPlayerFromServer = sender.getServer().getPlayer(target);
            if (targetPlayerFromServer != null) {
                targetPlayerFromServer.sendMessage(NationsPlusEconomy.walletPrefix +
                        "§eYou received §a" + NationsPlusEconomy.dollarFormat.format(receiverAmount) +
                        " §efrom §6[§r" + sender.getName() + "§6]§r, §ethe nation took §c$" + tax + "§e as tax!");
            }

        }

    }

}
