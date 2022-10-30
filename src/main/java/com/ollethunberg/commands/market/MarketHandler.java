package com.ollethunberg.commands.market;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.lib.CommandHandlerInterface;

public class MarketHandler implements CommandExecutor, CommandHandlerInterface {
    Market market = new Market();
    MarketGUI marketGUI = new MarketGUI();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                Integer arg0Integer = args.length > 0 ? Integer.parseInt(args[0]) : 0;
                switch (command.getName()) {
                    case "market":

                        marketGUI.openMarketGUI(player, arg0Integer);
                        break;
                    case "sell":
                        market.addMarketListing(player, arg0Integer);
                        break;
                    case "listings":
                        marketGUI.openListingsGUI(player, arg0Integer);
                        break;

                }
            } catch (SQLException e) {
                player.sendMessage("§cThere was an error while executing the command!");
                e.printStackTrace();

                return true;
            } catch (Error | Exception e) {
                player.sendMessage("§r[§4§lERROR§r]§c " + e.getMessage());

                return true;
            }
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
                "§c /market §r- Opens the market house",
                "§c /market sell <price> §r- Sells the item in your hand to the market");

    }

}
