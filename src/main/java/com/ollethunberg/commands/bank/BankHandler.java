package com.ollethunberg.commands.bank;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;

public class BankHandler implements CommandExecutor {

    Bank bank;

    public BankHandler() {

        bank = new Bank();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            /* Player sent the command */
            Player player = (Player) sender;
            if (args.length == 0) {
                try {
                    bank.bankAccount(player);
                } catch (SQLException e) {
                    player.sendMessage("§cAn error occured when trying to find your bank account!");
                    e.printStackTrace();
                }

                return true;
            }
            String action = args[0].toUpperCase();
            try {
                switch (action) {
                    case "CREATE": {
                        String bankName = args[1];
                        if (bankName == null)
                            throw new Error("You need to specify a bank name!");

                        bank.createBank(player, bankName);
                        break;
                    }

                    case "ACCOUNT": {
                        if (args.length == 1)
                            throw new Error(
                                    "You need to provide more arguments! §r§e/bank account <open|close> [bank name]");

                        String accountAction = args[1].toUpperCase();

                        switch (accountAction) {
                            case "OPEN":
                                String bankName = args[2];
                                if (bankName == null)
                                    throw new Error("You need to specify a bank name!");

                                bank.openBankAccount(player, bankName);
                                break;
                            case "CLOSE":
                                bank.closeBankAccount(player);
                                break;
                        }
                        break;
                    }
                    case "DEPOSIT": {
                        if (args.length == 1)
                            throw new Error("You need to provide an amount! §r§e/bank deposit <amount>");
                        float amount = Float.parseFloat(args[1]);
                        bank.deposit(player, amount);
                        break;
                    }
                    case "WITHDRAW": {
                        if (args.length == 1)
                            throw new Error("You need to provide an amount! §r§e/bank withdraw <amount>");
                        float amount = Float.parseFloat(args[1]);
                        bank.withdraw(player, amount);
                        break;
                    }
                    default: {
                        throw new Error("Invalid action! §r§e/bank <create|account|deposit|withdraw>");
                    }

                }
                return true;

            } catch (Error e) {
                player.sendMessage("§r[§4§lERROR§r]§c " + e.getMessage());
                return true;
            } catch (SQLException e) {
                player.sendMessage("§cThere was an error while executing the command!");
                e.printStackTrace();
                NationsPlusEconomy.LOGGER.warning(action + " " + e.getMessage());
                return true;
            }

        } else {
            /* Console sent the command */
            sender.sendMessage("You must be a player to use this command!");
        }
        return false;

    }

}
