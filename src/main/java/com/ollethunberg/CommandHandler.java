package com.ollethunberg;

import java.sql.Connection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.ollethunberg.commands.CommandBalance;

public class CommandHandler implements CommandExecutor {

    // This method is called, when somebody uses our command
    Connection conn;
    Plugin plugin = NationsPlusEconomy.getPlugin(NationsPlusEconomy.class);

    public CommandHandler(Connection _connection) {
        conn = _connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            String cmd = command.getName();
            Player executor = (Player) sender;
            if (cmd.equalsIgnoreCase("balance")) {
                if (args.length == 0) {
                    new CommandBalance(conn).execute(executor, executor.getName());
                    return true;
                } else if (args.length == 1) {
                    if (args[0] != null) {
                        new CommandBalance(conn).execute(executor, args[0]);
                        return true;
                    }
                }
            }
        } else {
            sender.sendMessage("You must be a player to use this command!");
        }

        return false;
    }
}
