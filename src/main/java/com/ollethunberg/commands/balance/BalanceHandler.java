package com.ollethunberg.commands.balance;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.lib.PermissionException;

public class BalanceHandler implements CommandExecutor {

    Balance balance;

    public BalanceHandler() {

        balance = new Balance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            /* Player sent the command */
            Player player = (Player) sender;
            try {

                if (args.length == 0) {
                    balance.balance(player, player.getName());
                    return true;
                } else if (args.length == 1) {
                    if (args[0] != null) {
                        balance.balance(player, args[0]);
                        return true;
                    }

                } else if (args[1].equalsIgnoreCase("give")) {
                    // args[0] = target
                    // args[1] = give
                    // args[2] = amount
                    if (args[0] != null && args[2] != null) {
                        balance.give(player, args[0], Float.parseFloat(args[2]));
                        return true;
                    }
                }
            } catch (PermissionException e) {
                player.sendMessage("§r[§4§lPERMISSION-ERROR§r]§c You don't have permission to execute this command");
            } catch (Error | Exception e) {
                player.sendMessage("§r[§4§lERROR§r]§c " + e.getMessage());
                return true;
            }
        } else {
            /* Console sent the command */
            sender.sendMessage("You must be a player to use this command!");
        }
        return false;

    }

}
