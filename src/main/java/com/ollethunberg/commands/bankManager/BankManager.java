package com.ollethunberg.commands.bankManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.entity.Player;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.commands.bank.BankHelper;
import com.ollethunberg.commands.bank.models.Bank;
import com.ollethunberg.commands.bank.models.PlayerBankAccount;
import com.ollethunberg.commands.loan.LoanHelper;
import com.ollethunberg.commands.loan.models.DBLoan;
import com.ollethunberg.lib.models.db.DBPlayer;
import com.ollethunberg.lib.helpers.PlayerHelper;
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

    public void deposit(Player executor, Integer amount) throws SQLException {
        Bank bank = bankHelper.getBankByOwnerPlayer(executor);
        DBPlayer player = playerHelper.getPlayer(executor.getUniqueId().toString());
        if (player.balance < amount) {
            executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cNot enough money!");
            return;
        }
        String query = "UPDATE bank SET balance = balance + ? WHERE bank_name = ?";
        update(query, amount, bank.bank_name);
        executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aDeposited "
                + NationsPlusEconomy.dollarFormat.format(amount) + " to the bank!");
        // update the balance of the player
        addBalancePlayer(executor.getUniqueId().toString(), -amount);
    }
    public void withdraw(Player executor, Integer amount) throws SQLException {
        Bank bank = bankHelper.getBankByOwnerPlayer(executor);
        if (bank.balance < amount) {
            executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cNot enough money in the bank!");
            return;
        }
        String query = "UPDATE bank SET balance = balance + ? WHERE bank_name = ?";
        update(query, -amount, bank.bank_name);
        executor.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aWithdrew "
                + NationsPlusEconomy.dollarFormat.format(amount) + " to the bank!");
        // update the balance of the player
        addBalancePlayer(executor.getUniqueId().toString(), amount);
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
        if (!authLoan(player, id))
            throw new Error("You are not authorized to tamper with this loan");
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
        if (!authLoan(player, id))
            throw new Error("You are not authorized to accept this loan");
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
        // update the balance of the bank
        String query = "UPDATE bank SET balance = balance - ? WHERE bank_name = ?";
        update(query, loanOffer.amount_total, bank.bank_name);
        addBalancePlayer(loanOffer.player_id, loanOffer.amount_total);
    }

    public void cancelLoan(Player player, int id) throws SQLException, Error {
        if (!authLoan(player, id))
            throw new Error("You are not authorized to cancel this loan");
        // Get the loan
        DBLoan loan = loanHelper.getLoanById(id);
        // check if the loan is active
        if (!loan.active || !loan.accepted) {
            player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§cThe loan is not active");
            return;
        }
        // cancel the loans, make it active=false and accepted=false
        String query = "UPDATE bank_loan SET active = ?, accepted = ? WHERE id = ?";
        update(query, false, false, id);
        player.sendMessage(NationsPlusEconomy.bankManagerPrefix + "§aLoan cancelled");

    }

    public void loanInfo(Player player, int id, String backCmd) throws Exception {
        if (!authLoan(player, id))
            throw new Error("You are not authorized to view this loan");
        // Get the loan
        DBLoan loan = loanHelper.getLoanById(id);
        // get the customer of the loan
        DBPlayer customer = playerHelper.getPlayer(loan.player_id);
        if (customer == null)
            throw new Error("Customer not found");
        bankManagerGUI.loanInfo(player, loan, customer.player_name, backCmd);
    }

    public void bankManager(Player player) throws Exception {
        // get the bank which the owner is in
        Bank bank = bankHelper.getBankByOwnerPlayer(player);
        bankManagerGUI.bankManager(player, bank);
    }

    public void getAccounts(Player player) throws Exception {
        // get the bank which the owner is in
        Bank bank = bankHelper.getBankByOwnerPlayer(player);
        List<PlayerBankAccount> accounts = bankHelper.getBankAccounts(bank.bank_name);

        bankManagerGUI.getAccounts(player, accounts);
    }

    public boolean authLoan(Player player, int loanId) throws SQLException, Error {
        // check if the player is the owner of the bank who has the loan
        DBLoan loan = loanHelper.getLoanById(loanId);
        Bank bank = bankHelper.getBank(loan.bank_name);
        if (!bank.owner.equals(player.getUniqueId().toString())) {

            return false;
        }
        return true;
    }

}
