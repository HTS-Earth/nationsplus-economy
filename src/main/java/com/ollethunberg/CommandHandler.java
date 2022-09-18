package com.ollethunberg;

import java.sql.Connection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.ollethunberg.commands.CommandBalance;
import com.ollethunberg.commands.CommandPay;

public class CommandHandler implements CommandExecutor {

    // This method is called, when somebody uses our command
    Connection conn;
    Plugin plugin = NationsPlusEconomy.getPlugin(NationsPlusEconomy.class);

    public CommandHandler(Connection _connection) {
        conn = _connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName();
        if (sender instanceof Player) {

            Player executor = (Player) sender;
            if (cmd.equalsIgnoreCase("balance")) {
                if (args.length == 0) {
                    new CommandBalance(conn).balance(executor, executor.getName());
                    return true;
                } else if (args.length == 1) {
                    if (args[0] != null) {
                        new CommandBalance(conn).balance(executor, args[0]);
                        return true;
                    }

                } else if (args[1].equalsIgnoreCase("give")) {
                    // args[0] = target
                    // args[1] = give
                    // args[2] = amount
                    if (args[0] != null && args[2] != null) {
                        new CommandBalance(conn).give(executor, args[0], Float.parseFloat(args[2]));
                        return true;
                    }
                }
            } else if (cmd.equalsIgnoreCase("pay")) {
                if (args.length == 2) {
                    if (args[0] != null && args[1] != null) {
                        new CommandPay(conn).pay(executor, args[0], Float.parseFloat(args[1]));
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
