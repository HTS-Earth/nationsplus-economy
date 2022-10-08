package com.ollethunberg.commands.bank;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.lib.CommandHandlerInterface;

public class BanksHandler implements CommandExecutor, CommandHandlerInterface {

    Bank bank;

    public BanksHandler() {

        bank = new Bank();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                    this.sendHelpMessage(player);
                    return true;
                }
                bank.listBanks(player);
                return true;
            } catch (Error e) {
                player.sendMessage("§r[§4§lERROR§r]§c " + e.getMessage());
                return true;
            } catch (SQLException e) {
                player.sendMessage("§cThere was an error while executing the command!");
                e.printStackTrace();
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
         * /banks shows all banks
         */
        player.sendMessage("Available commands:", "§c /banks §r§7- Shows all banks");
    }
}
