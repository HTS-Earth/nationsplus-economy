package com.ollethunberg.commands.loan;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.commands.bank.BankHelper;

import com.ollethunberg.commands.bank.models.PlayerBankInfo;
import com.ollethunberg.commands.loan.models.DBLoan;
import com.ollethunberg.lib.models.db.DBPlayer;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Loan extends WalletBalanceHelper {
        BankHelper bankHelper = new BankHelper();
        LoanHelper loanHelper = new LoanHelper();

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
                                                NationsPlusEconomy.loanPrefix + "§eLoan §a#"
                                                                + loansResultSet.getInt("id") + "§e (of §a"
                                                                + NationsPlusEconomy.dollarFormat.format(
                                                                                loansResultSet.getFloat("amount_total"))
                                                                + ")§e 100% paid off!");

                        } else if (!loansResultSet.getBoolean("accepted")) {
                                player.sendMessage(
                                                NationsPlusEconomy.loanPrefix + "§eLoan §a#"
                                                                + loansResultSet.getInt("id") + "§e (of §a"
                                                                + NationsPlusEconomy.dollarFormat.format(
                                                                                loansResultSet.getFloat("amount_total"))
                                                                + ")§e is pending approval!");

                        } else {
                                player.sendMessage(
                                                "--- Loan #" + loansResultSet.getInt("id") + " ---",
                                                "§eAmount: §a"
                                                                + NationsPlusEconomy.dollarFormat.format(loansResultSet
                                                                                .getFloat("amount_total")),
                                                "§eAmount Paid: §a"
                                                                + NationsPlusEconomy.dollarFormat.format(
                                                                                loansResultSet.getFloat("amount_paid")),
                                                "§eInterest: §a" + loansResultSet.getFloat("interest_rate") * 100 + "%",
                                                "§ePayments total: §a" + loansResultSet.getInt("payments_total"),
                                                "§ePayments left: §a" + loansResultSet.getInt("payments_left"));
                        }

                }

        }

        public void loanApplicationNew(Player player, String[] args) throws SQLException, Error {
                /*
                 * Command usage: /loan apply <amount> <interest rate> <payments quantity> Apply
                 * for a loan at your current bank
                 */
                // check that we can parse everything
                try {
                        // check that we have three arguments
                        if (args.length < 3)
                                throw new Error("Not enough arguments");

                        float amount = Float.parseFloat(args[0]);
                        if (amount < 1)
                                throw new Error("Amount must be greater than 0");
                        float interest_rate = Float.parseFloat(args[1]) / 100; // convert from user input to float.
                        if (interest_rate < 0)
                                throw new Error("You can not set a negative interest rate");

                        int payments_quantity = Integer.parseInt(args[2]);
                        if (payments_quantity < 1)
                                throw new Error("You can not set a negative amount of payments");

                        DBLoan newLoan = new DBLoan();

                        newLoan.amount_total = amount;
                        newLoan.payments_total = payments_quantity;
                        newLoan.payments_left = payments_quantity;
                        newLoan.interest_rate = interest_rate;
                        newLoan.player_id = player.getUniqueId().toString();

                        // get bank info from player
                        PlayerBankInfo playerBankInfo = bankHelper.getPlayerBankInfo(player);
                        newLoan.bank_name = playerBankInfo.bank_name;

                        loanHelper.createLoan(newLoan);

                        player.sendMessage(NationsPlusEconomy.loanPrefix
                                        + "§aLoan application sent! Please wait for approval by the bank's managers.");
                        // check if the bank manager is online
                        if (playerBankInfo.owner != null) {
                                // get the owner player
                                // get bank owner
                                DBPlayer owner = bankHelper.getBankOwner(playerBankInfo.bank_name);
                                Player ownerEntity = plugin.getServer().getPlayer(owner.player_name);
                                if (owner != null) {
                                        ownerEntity.sendMessage(NationsPlusEconomy.loanPrefix
                                                        + "§aYou have a new loan application from §e" + player.getName()
                                                        + "§a!");
                                }
                        }

                } catch (NumberFormatException e) {
                        throw new Error("Invalid number inputs. Please refer to the help command. /loan help");
                }

        }
}
