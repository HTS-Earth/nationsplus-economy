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
                this.sendHelpMessage(player);
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
                        switch (args.length) {
                            case 1: {
                                bankManager.listLoans(player, true, true);
                                break;
                            }
                            case 2: {
                                if (args[1].equalsIgnoreCase("offers")) {
                                    bankManager.listLoans(player, false, false);
                                }
                                break;
                            }
                            case 3: {
                                if (args[1].equalsIgnoreCase("offers")) {
                                    throw new Error("Usage is: /bankmanager loans offers <accept | deny>");
                                }
                                break;
                            }
                            case 4: {
                                String loanAction = args[2];
                                if (args[2] == null)
                                    throw new Error("You need to specify a loan id!");

                                int id = Integer.parseInt(args[3]);

                                if (loanAction.equalsIgnoreCase("accept")) {

                                    bankManager.acceptLoan(player, id);

                                } else if (loanAction.equalsIgnoreCase("deny")) {
                                    bankManager.updateOfferStatus(player, id, false);
                                }

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
        // see requested loans and accept or deny them
        player.sendMessage("§c/bankmanager loans offers" + "§7 - See the requested loans");
        player.sendMessage("§c/bankmanager loans" + "§7 - See active loans");
        player.sendMessage("§c/bankmanager loans offers accept [id]" + "§7 - Accept a loan");
        player.sendMessage("§c/bankmanager loans offers deny [id]" + "§7 - Deny a loan");
    }
}
