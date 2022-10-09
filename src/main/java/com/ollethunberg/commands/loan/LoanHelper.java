package com.ollethunberg.commands.loan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.ollethunberg.commands.loan.models.DBLoan;
import com.ollethunberg.lib.SQLHelper;

public class LoanHelper extends SQLHelper {

    public List<DBLoan> getLoansFromPlayer(Player player) throws SQLException {
        return getLoansFromPlayerId(player.getUniqueId().toString());
    }

    public List<DBLoan> getLoansFromPlayerId(String player_id) throws SQLException {
        ResultSet loan = query(
                "SELECT * from bank_loan where player_id=? order by id desc limit 27",
                player_id);
        // New list of loans with max size of 27
        List<DBLoan> loans = new ArrayList<DBLoan>(27);

        while (loan.next()) {
            DBLoan dbLoan = new DBLoan();
            dbLoan.id = loan.getInt("id");
            dbLoan.bank_name = loan.getString("bank_name");
            dbLoan.player_id = loan.getString("player_id");
            dbLoan.amount_paid = loan.getFloat("amount_paid");
            dbLoan.amount_total = loan.getFloat("amount_total");
            dbLoan.interest_rate = loan.getFloat("interest_rate");
            dbLoan.accepted = loan.getBoolean("accepted");
            dbLoan.active = loan.getBoolean("active");
            dbLoan.payments_total = loan.getInt("payments_total");
            dbLoan.payments_left = loan.getInt("payments_left");
            loans.add(dbLoan);
        }
        return loans;
    }
}
