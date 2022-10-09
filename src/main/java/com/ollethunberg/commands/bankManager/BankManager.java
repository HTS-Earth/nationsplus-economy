package com.ollethunberg.commands.bankManager;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.commands.bank.BankHelper;
import com.ollethunberg.commands.bank.models.Bank;
import com.ollethunberg.commands.loan.LoanHelper;
import com.ollethunberg.commands.loan.models.DBLoan;
import com.ollethunberg.utils.WalletBalanceHelper;

public class BankManager extends WalletBalanceHelper {
    LoanHelper loanHelper = new LoanHelper();
    BankHelper bankHelper = new BankHelper();

    public void setInterest(Player executor, Float amount) throws SQLException {
        if (amount < 0) {
            executor.sendMessage(NationsPlusEconomy.loanPrefix + "§cInterest can't be negative!");
            return;
        }
        // get the bank which the owner is in
        ResultSet bank = getBank(executor);
        String bankName = bank.getString("bank_name");
        // set the interest rate
        String query = "UPDATE bank SET saving_interest = ? WHERE bank_name = ?";
        update(query, amount, bankName);
        executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aInterest rate set to " + amount * 100 + "%");
    }

    public void getInterest(Player executor) throws SQLException {
        // get the bank which the owner is in
        ResultSet bank = getBank(executor);
        Float interest = bank.getFloat("saving_interest");
        executor.sendMessage(
                NationsPlusEconomy.bankManagerPrefix + "§aSaving interest rate is " + interest * 100 + "%");
    }

    public ResultSet getBank(Player executor) throws SQLException {
        ResultSet bank = query("SELECT * FROM bank WHERE owner = ?", executor.getUniqueId().toString());
        if (bank.next()) {
            return bank;
        } else {
            return null;
        }
    }

    public void listLoans(Player executor, boolean active, boolean accepted) throws SQLException {
        // get the bank which the owner is in
        ResultSet bank = getBank(executor);
        String bankName = bank.getString("bank_name");
        // get the loans
        String query = "SELECT bl.*, p.player_name FROM bank_loan as bl inner join player as p on p.uid = bl.player_id WHERE bl.bank_name = ? and bl.active = ? and bl.accepted = ?";
        ResultSet loans = query(query, bankName, active, accepted);
        // print the loans
        try {
            executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aLoans:");
            int amountOfLoans = 0;
            while (loans.next()) {
                executor.sendMessage("§r§a---------------------");
                executor.sendMessage("§r§eID: §r§2" + loans.getInt("id"));
                executor.sendMessage("§r§ePlayer: §r§e" + loans.getString("player_name"));
                executor.sendMessage("§r§eAmount paid: §r§a"
                        + NationsPlusEconomy.dollarFormat.format(loans.getFloat("amount_paid")));
                executor.sendMessage("§r§eAmount total: §r§a"
                        + NationsPlusEconomy.dollarFormat.format(loans.getFloat("amount_total")));
                executor.sendMessage("§r§eInterest: §r§a" + loans.getFloat("interest_rate") * 100 + "§r%");
                executor.sendMessage("§r§ePayments left: §r§e" + loans.getInt("payments_left"));
                executor.sendMessage("§r§ePayments total: §r§e" + loans.getInt("payments_total"));
                executor.sendMessage(
                        "§r§ePer hour: §r§a" + NationsPlusEconomy.dollarFormat
                                .format(loans.getFloat("amount_total") / loans.getInt("payments_total")
                                        + ((loans.getFloat("amount_total") - loans.getFloat("amount_paid"))
                                                * loans.getFloat("interest_rate"))));
                executor.sendMessage("§r§a---------------------");
                amountOfLoans++;
            }

            if (amountOfLoans == 0) {
                executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§r§cNo loans found");
            } else {
                executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aFound " + amountOfLoans + " loans");
            }

        } catch (SQLException e) {
            executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cThere was an error while listing the loans");
            e.printStackTrace();
        }
    }

    public void updateOfferStatus(Player player, int id, boolean accepted) throws SQLException, Error {

        // get the bank which the owner is in
        ResultSet bank = getBank(player);
        String bankName = bank.getString("bank_name");
        // check if the offer exists and if it has active=false;
        String query = "SELECT * FROM bank_loan WHERE id = ? and bank_name = ? and active = ? and accepted = ?";
        ResultSet offer = query(query, id, bankName, false, false);

        if (offer.next()) {
            // update the offer
            if (accepted) {
                query = "UPDATE bank_loan SET accepted = ?, active = ? WHERE id = ?";
                update(query, true, true, id);
            } else {
                // delete the offer
                query = "DELETE FROM bank_loan WHERE id = ?";
                update(query, id);
            }

            player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aOffer updated");
        } else {

            throw new Error("Offer not found");
        }

    }

    public void acceptLoan(Player player, int id) throws SQLException, Error {
        // Get the loan from the database
        DBLoan loanOffer = loanHelper.getLoanById(id);
        Bank bank = bankHelper.getBank(loanOffer.bank_name);

        // check if the bank has enough money
        if (bank.balance < loanOffer.amount_total) {
            player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cThe bank does not have enough money");
            return;
        }
        // Pay out the loan to the player and notifiy them

        updateOfferStatus(player, id, true);
    }

}
