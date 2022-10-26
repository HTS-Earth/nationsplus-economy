package com.ollethunberg.commands.bankManager;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;

public class BankManagerHandler implements CommandExecutor {
    BankManager bankManager;

    public BankManagerHandler() {
        bankManager = new BankManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {

            Player player = (Player) sender;
            if (args.length == 0) {
                try {
                    bankManager.bankManager(player);
                } catch (Exception e) {
                    player.sendMessage("§cAn error occured when trying to find your bank!");
                    e.printStackTrace();
                }
                return true;
            }
            String action = args[0].toUpperCase();
            try {
                switch (action) {

                    case "INTEREST": {
                        if (args.length == 1) {
                            bankManager.getInterest(player);
                            break;
                        } else {

                            Float interest = Float.parseFloat(args[1]) / 100;

                            bankManager.setInterest(player, interest);
                            break;
                        }
                    }
                    case "LOANS": {
                        if (args.length == 1) {
                            bankManager.listLoans(player, true, true);
                        }
                        String loansArg = args[1].toUpperCase();

                        switch (loansArg) {
                            case "OFFERS": {
                                if (args.length == 2) {
                                    bankManager.listLoans(player, false, false);
                                } else {
                                    String offersAction = args[2].toUpperCase();
                                    if (offersAction.equals("ACCEPT")) {
                                        bankManager.acceptLoan(player, Integer.parseInt(args[3]));
                                    } else if (offersAction.equals("DECLINE")) {
                                        bankManager.updateOfferStatus(player, Integer.parseInt(args[3]), false);
                                    }
                                }
                                break;
                            }
                            case "INFO": {
                                bankManager.loanInfo(player, Integer.parseInt(args[2]), args.length > 3 ? args[3] : "");
                                break;
                            }
                            case "CANCEL": {
                                bankManager.cancelLoan(player, Integer.parseInt(args[2]));
                                break;
                            }
                        }
                        break;
                    }
                    default: {
                        this.sendHelpMessage(player);
                        break;
                    }
                }
                return true;
            } catch (SQLException e) {
                player.sendMessage("§cThere was an error while executing the command!");
                e.printStackTrace();
                NationsPlusEconomy.LOGGER.warning(action + " " + e.getMessage());
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage("§r[§4§lNUMBER-ERROR§r]§c Please provide valid numbers!");
                return true;
            } catch (Error | Exception e) {
                player.sendMessage("§r[§4§lERROR§r]§c " + e.getMessage());
                return true;
            }

        } else {
            sender.sendMessage("You need to be a player to use this command!");
        }
        return false;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("Available commands: ");
        player.sendMessage(
                "§c/bankmanager interest [amount in %]" + "§7 - Set the interest rate for the bank");
        player.sendMessage("§c/bankmanager interest" + "§7 - Get the interest rate for the bank");
        // see requested loans and accept or decline them
        player.sendMessage("§c/bankmanager loans offers" + "§7 - See the requested loans");
        player.sendMessage("§c/bankmanager loans" + "§7 - See active loans");
        player.sendMessage("§c/bankmanager loans offers accept [id]" + "§7 - Accept a loan");
        player.sendMessage("§c/bankmanager loans offers decline [id]" + "§7 - Decline a loan");
        player.sendMessage("§c/bankmanager loans cancel [id]" + "§7 - Cancel an accepted and active loan");
        player.sendMessage("§c/bankmanager help §7 - Shows help message");
    }
}
