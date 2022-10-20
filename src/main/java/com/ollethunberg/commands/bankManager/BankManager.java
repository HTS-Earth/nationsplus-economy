package com.ollethunberg.commands.bankManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.commands.bank.BankHelper;
import com.ollethunberg.commands.bank.models.Bank;
import com.ollethunberg.commands.loan.LoanHelper;
import com.ollethunberg.commands.loan.models.DBLoan;
import com.ollethunberg.database.DBPlayer;
import com.ollethunberg.lib.PlayerHelper;
import com.ollethunberg.utils.WalletBalanceHelper;

public class BankManager extends WalletBalanceHelper {
    LoanHelper loanHelper = new LoanHelper();
    BankHelper bankHelper = new BankHelper();
    BankManagerGUI bankManagerGUI = new BankManagerGUI();
    PlayerHelper playerHelper = new PlayerHelper();

    public void setInterest(Player executor, Float amount) throws SQLException {
        if (amount < 0) {
            executor.sendMessage(NationsPlusEconomy.loanPrefix + "§cInterest can't be negative!");
            return;
        }
        // get the bank which the owner is in
        Bank bank = bankHelper.getBankByOwnerPlayer(executor);

        // set the interest rate
        String query = "UPDATE bank SET saving_interest = ? WHERE bank_name = ?";
        update(query, amount, bank.bank_name);
        executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aInterest rate set to " + amount * 100 + "%");
    }

    public void getInterest(Player executor) throws SQLException {
        // get the bank which the owner is in
        Bank bank = bankHelper.getBankByOwnerPlayer(executor);
        Float interest = bank.saving_interest;
        executor.sendMessage(
                NationsPlusEconomy.bankManagerPrefix + "§aSaving interest rate is " + interest * 100 + "%");
    }

    public void listLoans(Player executor, boolean active, boolean accepted) throws SQLException, Exception {
        // get the bank which the owner is in
        Bank bank = bankHelper.getBankByOwnerPlayer(executor);

        // get the loans
        String query = "SELECT bl.*, p.player_name FROM bank_loan as bl inner join player as p on p.uid = bl.player_id WHERE bl.bank_name = ? and bl.active = ? and bl.accepted = ?";
        ResultSet loans = query(query, bank.bank_name, active, accepted);
        // serialize the loans
        List<DBLoan> loansArray = loanHelper.serializeDBLoans(loans);
        // get all the players from the loans
        ResultSet players = query(
                "SELECT * FROM player where uid in (SELECT p.uid FROM bank_loan as bl inner join player as p on p.uid = bl.player_id WHERE bl.bank_name = ? and bl.active = ? and bl.accepted = ?)",
                bank.bank_name, active, accepted);
        // serialize the players
        List<DBPlayer> playersArray = playerHelper.serializeDBPlayers(players);
        // print the loans

        bankManagerGUI.listLoans(executor, loansArray, playersArray);
    }

    public void updateOfferStatus(Player player, int id, boolean accepted) throws SQLException, Error {

        // get the bank which the owner is in
        Bank bank = bankHelper.getBankByOwnerPlayer(player);

        // check if the offer exists and if it has active=false;
        String query = "SELECT * FROM bank_loan WHERE id = ? and bank_name = ? and active = ? and accepted = ?";
        ResultSet offer = query(query, id, bank.bank_name, false, false);

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
