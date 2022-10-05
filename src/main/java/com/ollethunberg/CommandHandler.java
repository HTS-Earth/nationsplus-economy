package com.ollethunberg;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.ollethunberg.commands.balance.BalanceHandler;
import com.ollethunberg.commands.bank.Bank;
import com.ollethunberg.commands.pay.Pay;

public class CommandHandler implements CommandExecutor {

    // This method is called, when somebody uses our command
    Connection conn;
    Plugin plugin = NationsPlusEconomy.getPlugin(NationsPlusEconomy.class);

    BalanceHandler balanceHandler;

    public CommandHandler(Connection _connection) {
        conn = _connection;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName();
        if (sender instanceof Player) {

            Player executor = (Player) sender;
            if (cmd.equalsIgnoreCase("pay")) {
                if (args.length == 2) {
                    if (args[0] != null && args[1] != null) {
                        new Pay(conn).pay(executor, args[0], Float.parseFloat(args[1]));
                        return true;
                    }
                }
            } else if (cmd.equalsIgnoreCase("banks")) {
                try {
                    new Bank(conn).listBanks(executor);
                    return true;
                } catch (SQLException e) {
                    // give the error to the user
                    executor.sendMessage("§cAn error occured while listing the banks!");
                    e.printStackTrace();
                    return false;
                }
            }

        }

        return false;
    }
}
