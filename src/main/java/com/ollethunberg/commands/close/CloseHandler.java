package com.ollethunberg.commands.close;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.lib.CommandHandlerInterface;

public class CloseHandler implements CommandExecutor, CommandHandlerInterface {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.closeInventory();
            return true;
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
                "§c /close §r- Closes the current inventory");

    }

}
