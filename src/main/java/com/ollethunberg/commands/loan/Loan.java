package com.ollethunberg.commands.loan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Loan extends WalletBalanceHelper {
    public Loan(Connection _connection) {
        super(_connection);
    }

    /* Loans command, gets current loans. */
    public void loans(Player player) {
        try {
            ResultSet loansResultSet = query("SELECT * from bank_loan where player_id = ? ORDER BY id DESC",
                    player.getUniqueId().toString());
            if (!loansResultSet.next()) {
                player.sendMessage(NationsPlusEconomy.bankPrefix + "§cYou don't have any loans!");
                return;
            }
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
                            "--- Loan #" + loansResultSet.getInt("loan_id") + " ---",
                            "§eAmount: §a"
                                    + NationsPlusEconomy.dollarFormat.format(loansResultSet.getFloat("amount_total")),
                            "§eAmount Paid: §a"
                                    + NationsPlusEconomy.dollarFormat.format(loansResultSet.getFloat("amount_total")),
                            "§eInterest: §a" + loansResultSet.getFloat("interest_rate") * 100 + "%",
                            "§ePayments total: §a" + loansResultSet.getInt("payments_total"),
                            "§ePayments left: §a" + loansResultSet.getInt("payments_left"));
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
