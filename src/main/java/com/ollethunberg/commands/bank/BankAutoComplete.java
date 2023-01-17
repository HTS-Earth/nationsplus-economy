package com.ollethunberg.commands.bank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.ollethunberg.NationsPlusEconomy;

public class BankAutoComplete implements TabCompleter {

    BankHelper bankHelper = new BankHelper();
    public static ArrayList<String> nations = new ArrayList<String>();

    private static final String[] keywords = { "deposit", "withdraw", "account", "create" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 1) {
                return Arrays.asList(keywords);
            } else if (args.length == 2) {
                switch (args[0]) {
                    case "account":
                        String[] accountKeywords = { "open", "close" };
                        return Arrays.asList(accountKeywords);
                    case "create":
                        String[] createKeywords = { "<bank name>" };
                        return Arrays.asList(createKeywords);
                    case "deposit":
                        String[] depositKeywords = { "<amount>" };
                        return Arrays.asList(depositKeywords);
                    case "withdraw":
                        String[] withdrawKeywords = { "<amount>" };
                        return Arrays.asList(withdrawKeywords);
                }

            } else if (args.length == 3) {
                switch (args[0]) {
                    case "account":
                        if (args[1].equals("open")) {
                            // get all banks
                            try {
                                List<com.ollethunberg.commands.bank.models.Bank> banks = bankHelper.getBanks();
                                // get all the names of the banks using stream
                                String[] banksNames = banks.stream().map(bank -> bank.bank_name).toArray(String[]::new);
                                return Arrays.asList(banksNames);
                            } catch (SQLException e) {
                                NationsPlusEconomy.LOGGER.info("Error while getting banks: " + e.getMessage());
                            }
                        }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
