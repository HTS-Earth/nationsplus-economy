package com.ollethunberg.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ollethunberg.lib.SQLHelper;

public class WalletBalanceHelper extends SQLHelper {
    public WalletBalanceHelper(Connection conn) {
        super(conn);
    }

    public float addBalancePlayer(String uid, float amount) throws SQLException, Error {
        // get the player id and check their balance.

        ResultSet currentPlayerResultSet = query("SELECT balance from player where uid = ?;", uid);
        if (currentPlayerResultSet.next()) {
            float currentBalance = currentPlayerResultSet.getFloat("balance");
            float newBalance = currentBalance + amount;
            if (newBalance < 0) {
                throw new Error("Balance cannot be negative");
            } else {
                update("UPDATE player SET balance = ? WHERE uid = ?;", newBalance, uid);
                return newBalance;
            }

        } else {
            throw new Error("Player not found");
        }

    }

    // addBalanceNation
    public float addBalanceNation(String nation_name, float amount) throws SQLException, Error {
        ResultSet currentNationResultSet = query("SELECT balance from nation where name = ?;", nation_name);
        if (currentNationResultSet.next()) {
            float currentBalance = currentNationResultSet.getFloat("balance");
            float newBalance = currentBalance + amount;
            if (newBalance < 0) {
                throw new Error("Balance cannot be negative");
            } else {
                update("UPDATE nation SET balance = ? WHERE name = ?;", newBalance, nation_name);
                return newBalance;
            }

        } else {
            throw new Error("Nation not found");
        }
    }

    public float getBalance(String target, boolean useId) throws SQLException {
        // check if the target is a player or nation
        String sql;
        if (useId)
            sql = "SELECT uid,balance from player where uid = ?;";
        else
            sql = "SELECT uid,balance from player where LOWER(player_name) = LOWER(?)";

        ResultSet playerResultSet = query(sql, target);
        if (playerResultSet.next()) {
            return playerResultSet.getFloat("balance");
        } else {
            ResultSet nationResultSet = query("SELECT name,balance from nation where LOWER(name) = LOWER(?)", target);
            if (nationResultSet.next()) {
                return nationResultSet.getFloat("balance");
            } else {
                throw new Error("That player or nation doesn't exist!");
            }

        }
    }

    public String getEntityType(String target) throws SQLException, Error {
        // check if the target is a player or nation
        ResultSet playerResultSet = query("SELECT uid,balance from player where LOWER(player_name) = LOWER(?)", target);
        if (playerResultSet.next()) {
            return "player";
        } else {
            ResultSet nationResultSet = query("SELECT name,balance from nation where LOWER(name) = LOWER(?)", target);
            if (nationResultSet.next()) {
                return "nation";
            } else {
                throw new Error("That player or nation doesn't exist!");
            }

        }
    }
}
