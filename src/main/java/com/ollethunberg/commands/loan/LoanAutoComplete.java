package com.ollethunberg.commands.loan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.ollethunberg.lib.helpers.NationHelper;

public class LoanAutoComplete implements TabCompleter {
    NationHelper nationHelper;
    public static ArrayList<String> nations = new ArrayList<String>();

    private static final String[] keywords = { "apply", "<help>" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 1) {
                return Arrays.asList(keywords);
            } else if (args.length == 2) {
                switch (args[0]) {
                    case "apply":
                        String[] applyKeywords = { "<amount>" };
                        return Arrays.asList(applyKeywords);
                }
            } else if (args.length == 3) {
                switch (args[0]) {
                    case "apply":
                        String[] interestRateKeyWords = { "<interest rate>" };
                        return Arrays.asList(interestRateKeyWords);
                }
            } else if (args.length == 4) {
                switch (args[0]) {
                    case "apply":
                        String[] paymentQuantityKeyWords = { "<payment quantity>" };
                        return Arrays.asList(paymentQuantityKeyWords);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
