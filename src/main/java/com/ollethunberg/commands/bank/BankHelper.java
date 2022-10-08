package com.ollethunberg.commands.bank;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ollethunberg.commands.bank.classes.PlayerBankAccount;
import com.ollethunberg.commands.bank.classes.PlayerBankInfo;
import com.ollethunberg.lib.SQLHelper;

public class BankHelper extends SQLHelper {

    public PlayerBankInfo getPlayerBankInfo(Player player) throws SQLException {
        ResultSet bankInfo = query(
                "SELECT b.*, (SELECT SUM(balance) from bank_account where bank_name=b.bank_name) as customers_balance, (select count(*) from bank_account where bank_name=b.bank_name) as customers from bank as b inner join bank_account as ba on ba.bank_name = b.bank_name where ba.player_id=?",
                player.getUniqueId().toString());
        // serialize bank info
        PlayerBankInfo playerBankInfo = new PlayerBankInfo();
        if (bankInfo.next()) {
            playerBankInfo.bank_name = bankInfo.getString("bank_name");
            playerBankInfo.owner = bankInfo.getString("owner");
            playerBankInfo.saving_interest = bankInfo.getFloat("saving_interest");
            playerBankInfo.balance = bankInfo.getFloat("balance");
            playerBankInfo.customers_balance = bankInfo.getFloat("customers_balance");
            playerBankInfo.customers = bankInfo.getInt("customers");
            playerBankInfo.safety_rating = (playerBankInfo.balance / playerBankInfo.customers_balance) * 100;
        }
        return playerBankInfo;
    }

    public PlayerBankAccount getPlayerBankAccount(Player player) throws SQLException {
        ResultSet bankAccount = query(
                "SELECT * from bank_account where player_id=?",
                player.getUniqueId().toString());
        // serialize bank info
        PlayerBankAccount playerBankAccount = new PlayerBankAccount();
        if (bankAccount.next()) {
            playerBankAccount.bank_name = bankAccount.getString("bank_name");
            playerBankAccount.player_id = bankAccount.getString("player_id");
            playerBankAccount.balance = bankAccount.getFloat("balance");
        }
        return playerBankAccount;
    }

}
