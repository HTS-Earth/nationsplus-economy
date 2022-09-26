package com.ollethunberg.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.utils.BalanceHelper;

public class CommandBankManager extends BalanceHelper {
    public CommandBankManager(Connection conn) {
        super(conn);
    }
    /*
     * executor.sendMessage("Available commands: ");
     * executor.sendMessage("§c/bankmanager interest [amount in %]" +
     * "§7 - Set the interest rate for the bank");
     * executor.sendMessage("§c/bankmanager interest" +
     * "§7 - Get the interest rate for the bank");
     * // see requested loans and accept or deny them
     * executor.sendMessage("§c/bankmanager loans" +
     * "§7 - See the requested loans");
     * executor.sendMessage("§c/bankmanager loans accept [id]" +
     * "§7 - Accept a loan");
     * executor.sendMessage("§c/bankmanager loans deny [id]" + "§7 - Deny a loan");
     */

    public void setInterest(Player executor, Float amount) throws SQLException {

        // get the bank which the owner is in
        ResultSet bank = getBank(executor);
        String bankName = bank.getString("bank_name");
        // set the interest rate
        String query = "UPDATE bank SET saving_interest = ? WHERE bank_name = ?";
        update(query, amount, bankName);
        executor.sendMessage("§aInterest rate set to " + amount * 100 + "%");
    }

    public void getInterest(Player executor) throws SQLException {
        // get the bank which the owner is in
        ResultSet bank = getBank(executor);
        Float interest = bank.getFloat("saving_interest");
        executor.sendMessage("§aSaving interest rate is " + interest * 100 + "%");
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
            int amountOfLoans = 0;
            while (loans.next()) {
                executor.sendMessage("§r§a---------------------");
                executor.sendMessage("§r§eID: §r§2" + loans.getInt("id"));
                executor.sendMessage("§r§ePlayer: §r§e" + loans.getString("player_name"));
                executor.sendMessage("§r§eAmount paid: §r§a$" + loans.getFloat("amount_paid"));
                executor.sendMessage("§r§eAmount total: §r§a$" + loans.getFloat("amount_total"));
                executor.sendMessage("§r§eInterest: §r§a" + loans.getFloat("interest_rate") * 100 + "§r%");
                executor.sendMessage("§r§ePayments left: §r§e" + loans.getInt("payments_left"));
                executor.sendMessage("§r§ePayments total: §r§e" + loans.getInt("payments_total"));
                executor.sendMessage(
                        "§r§ePer hour: §r§a$" + loans.getFloat("amount_total") / loans.getInt("payments_total"));
                executor.sendMessage("§r§a---------------------");
                amountOfLoans++;
            }
            if (amountOfLoans == 0) {
                executor.sendMessage("§r§cNo loans found");
            } else {
                executor.sendMessage("§aFound " + amountOfLoans + " loans");
            }

        } catch (SQLException e) {
            executor.sendMessage("§cThere was an error while listing the loans");
            e.printStackTrace();
        }
    }

    public void updateOfferStatus(Player player, int id, boolean accepted) throws SQLException {

        // get the bank which the owner is in
        ResultSet bank = getBank(player);
        String bankName = bank.getString("bank_name");
        // check if the offer exists and if it has active=false;
        String query = "SELECT * FROM bank_loan WHERE id = ? and bank_name = ? and active = ?";
        ResultSet offer = query(query, id, bankName, false);
        if (offer.next()) {
            // update the offer
            query = "UPDATE bank_loan SET accepted = ?, active = ? WHERE id = ?";
            update(query, accepted, true, id);
            player.sendMessage("§aOffer updated");
        } else {
            player.sendMessage("§cOffer not found");
        }

    }

}
