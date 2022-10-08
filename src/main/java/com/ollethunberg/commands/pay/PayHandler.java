package com.ollethunberg.commands.pay;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.lib.CommandHandlerClass;

public class PayHandler implements CommandExecutor, CommandHandlerClass {

    Pay pay;

    public PayHandler() {

        pay = new Pay();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                if (args.length == 0) {
                    this.sendHelpMessage(player);
                    return true;
                }

                if (args[0] == null || args[1] == null) {
                    sendHelpMessage(player);
                    return true;
                }
                String target = args[0];
                Float amount = Float.parseFloat(args[1]);

                pay.pay(player, target, amount);

                // pay <player> <amount>
                return true;
            } catch (Error e) {
                player.sendMessage("§r[§4§lERROR§r]§c " + e.getMessage());
                return true;
            } catch (SQLException e) {
                player.sendMessage("§cThere was an error while executing the command!");
                e.printStackTrace();
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage("§r[§4§lNUMBER-ERROR§r]§c Please provide valid numbers!");
                return true;
            }
        } else {
            sender.sendMessage("§cYou need to be a player to use this command!");
            return true;
        }
    }

    @Override
    public void sendHelpMessage(Player player) {
        /*
         * /loans shows all your loans
         * 
         */
        player.sendMessage(
                "Available commands:",
                "§c /pay <player> <amount> §r- §7Pay a player an amount of money§r");

    }

}
