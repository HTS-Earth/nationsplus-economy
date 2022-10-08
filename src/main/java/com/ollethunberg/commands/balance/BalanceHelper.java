package com.ollethunberg.commands.balance;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.commands.balance.models.Balance;
import com.ollethunberg.lib.SQLHelper;

public class BalanceHelper extends SQLHelper {

    public Balance getBalanceFromPlayer(Player player) throws SQLException, Error {
        return this.getBalance(player.getUniqueId().toString());
    }

    public Balance getBalance(String player_id) throws SQLException, Error {
        ResultSet balanceResultSet = query(
                "SELECT p.balance, (select balance from bank_account where player_id=p.uid) as bank_balance from player as p where p.uid = ?",
                player_id);

        if (!balanceResultSet.next())
            throw new Error("Player not found in database");

        Balance balance = new Balance();
        balance.player_id = player_id;
        balance.balance = balanceResultSet.getFloat("balance");
        balance.bank_balance = balanceResultSet.getFloat("bank_balance");
        return balance;

    }
}
