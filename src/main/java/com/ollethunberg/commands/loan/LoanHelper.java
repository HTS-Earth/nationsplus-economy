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
            loans.add(serializeDBLoan(loan));
        }
        return loans;
    }

    public DBLoan serializeDBLoan(ResultSet dbData) throws SQLException {
        DBLoan dbLoan = new DBLoan();
        dbLoan.id = dbData.getInt("id");
        dbLoan.bank_name = dbData.getString("bank_name");
        dbLoan.player_id = dbData.getString("player_id");
        dbLoan.amount_paid = dbData.getFloat("amount_paid");
        dbLoan.amount_total = dbData.getFloat("amount_total");
        dbLoan.interest_rate = dbData.getFloat("interest_rate");
        dbLoan.accepted = dbData.getBoolean("accepted");
        dbLoan.active = dbData.getBoolean("active");
        dbLoan.payments_total = dbData.getInt("payments_total");
        dbLoan.payments_left = dbData.getInt("payments_left");
        return dbLoan;
    }

    public float getCostPerHour(DBLoan loan) {
        return loan.amount_total / loan.payments_total
                + (loan.amount_total - loan.amount_paid) * loan.interest_rate;
    }

    public DBLoan createLoan(DBLoan loan) throws SQLException {
        // insert the loan to the database
        String createLoanSQL = "INSERT INTO \"public\".\"bank_loan\" (\"bank_name\", \"player_id\", \"amount_total\", \"amount_paid\", \"interest_rate\", \"accepted\", \"active\", \"payments_left\", \"payments_total\") VALUES (?,?, ?,0, ?, false, false,0, ?) RETURNING *;";
        ResultSet newLoan = query(createLoanSQL, loan.bank_name, loan.player_id, loan.amount_total, loan.interest_rate,
                loan.payments_total);
        if (!newLoan.next())
            throw new Error("There was an error creating the loan.");
        return serializeDBLoan(newLoan);

    }

    public DBLoan getLoanById(int id) throws SQLException {
        ResultSet loan = query("SELECT * from bank_loan where id=?", id);
        if (!loan.next())
            throw new Error("There is no loan with that id.");
        return serializeDBLoan(loan);
    }
}
