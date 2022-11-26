package com.ollethunberg.commands.auction;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.lib.PermissionException;

public class AuctionHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Auction auction = new Auction();
        if (sender instanceof Player) {
            /* Player sent the command */
            Player player = (Player) sender;

            try {
                String cmd = command.getName();
                switch (cmd) {
                    // command for placing bids
                    case "bid":
                        auction.bid(player, Integer.parseInt(args[0]));
                        break;
                    // put an item up for auction
                    case "auction":
                        auction.auction(player);
                        break;
                    default:
                        break;
                }
            } catch (SQLException e) {
                player.sendMessage("§cAn error occured when trying to access the database!");
                e.printStackTrace();
            } catch (PermissionException e) {
                player.sendMessage("§r[§4§lPERMISSION-ERROR§r]§c You don't have permission to execute this command");
            } catch (Error | Exception e) {
                player.sendMessage("§r[§4§lERROR§r]§c " + e.getMessage());
            }

            return true;

        } else {
            sender.sendMessage("§cYou need to be a player to use this command!");
            return true;
        }

    }
}
