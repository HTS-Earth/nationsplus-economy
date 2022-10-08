package com.ollethunberg.commands.loan;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Loan extends WalletBalanceHelper {

    /* Loans command, gets current loans. */
    public void loans(Player player) throws SQLException {

        ResultSet loansResultSet = query("SELECT * from bank_loan where player_id = ? ORDER BY id ASC",
                player.getUniqueId().toString());

        // get length of resultSet

        player.sendMessage("§r§a-- " + NationsPlusEconomy.loanPrefix + "§a Loans --");
        /* List all loans to player */
        while (loansResultSet.next()) {
            if (loansResultSet.getBoolean("accepted") && !loansResultSet.getBoolean("active")) {
                player.sendMessage(
                        NationsPlusEconomy.loanPrefix + "§eLoan §a#" + loansResultSet.getInt("id") + "§e (of §a"
                                + NationsPlusEconomy.dollarFormat.format(loansResultSet.getFloat("amount_total"))
                                + ")§e 100% paid off!");

            } else if (!loansResultSet.getBoolean("accepted")) {
                player.sendMessage(
                        NationsPlusEconomy.loanPrefix + "§eLoan §a#" + loansResultSet.getInt("id") + "§e (of §a"
                                + NationsPlusEconomy.dollarFormat.format(loansResultSet.getFloat("amount_total"))
                                + ")§e is pending approval!");

            } else {
                player.sendMessage(
                        "--- Loan #" + loansResultSet.getInt("id") + " ---",
                        "§eAmount: §a"
                                + NationsPlusEconomy.dollarFormat.format(loansResultSet.getFloat("amount_total")),
                        "§eAmount Paid: §a"
                                + NationsPlusEconomy.dollarFormat.format(loansResultSet.getFloat("amount_paid")),
                        "§eInterest: §a" + loansResultSet.getFloat("interest_rate") * 100 + "%",
                        "§ePayments total: §a" + loansResultSet.getInt("payments_total"),
                        "§ePayments left: §a" + loansResultSet.getInt("payments_left"));
            }

        }

    }
}
