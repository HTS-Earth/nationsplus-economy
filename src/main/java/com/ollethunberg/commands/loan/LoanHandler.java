package com.ollethunberg.commands.loan;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.lib.CommandHandlerInterface;

public class LoanHandler implements CommandExecutor, CommandHandlerInterface {

    Loan loan;

    public LoanHandler() {

        loan = new Loan();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                if (args.length == 0) {
                    loan.loans(player);
                    return true;
                }
                String action = args[0].toUpperCase();
                switch (action) {
                    case "HELP": {
                        this.sendHelpMessage(player);
                        break;
                    }
                }
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
                "§r§c/loans §r§7Shows all your loans"

        );
    }

}
