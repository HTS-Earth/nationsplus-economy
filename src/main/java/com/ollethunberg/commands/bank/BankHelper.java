package com.ollethunberg.commands.bank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.ollethunberg.commands.bank.models.PlayerBankAccount;
import com.ollethunberg.commands.bank.models.PlayerBankInfo;
import com.ollethunberg.commands.bank.models.Bank;
import com.ollethunberg.lib.models.db.DBPlayer;
import com.ollethunberg.lib.helpers.SQLHelper;

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
            playerBankInfo.safety_rating = (playerBankInfo.balance / playerBankInfo.customers_balance);
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

    public Bank serializeBank(ResultSet rs) throws SQLException {
        Bank bank = new Bank();
        bank.bank_name = rs.getString("bank_name");
        bank.owner = rs.getString("owner");
        bank.saving_interest = rs.getFloat("saving_interest");
        bank.balance = rs.getFloat("balance");
        bank.customers_balance = rs.getFloat("customers_balance");
        bank.safety_rating = bank.customers_balance / bank.balance;
        return bank;
    }

    public List<Bank> serializeBanks(ResultSet rs) throws SQLException {
        List<Bank> banks = new ArrayList<Bank>();
        while (rs.next()) {
            banks.add(serializeBank(rs));
        }
        return banks;
    }

    public DBPlayer getBankOwner(String bank_name) throws SQLException {
        ResultSet bankOwner = query(
                "SELECT p.* from bank as b inner join player as p on p.uid=b.owner where b.bank_name=?",
                bank_name);
        // serialize bank info
        DBPlayer dbPlayer = new DBPlayer();
        if (bankOwner.next()) {
            dbPlayer.uid = bankOwner.getString("uid");
            dbPlayer.player_name = bankOwner.getString("player_name");
            dbPlayer.balance = bankOwner.getFloat("balance");
            dbPlayer.nation = bankOwner.getString("nation");
            dbPlayer.kills = bankOwner.getInt("kills");
            dbPlayer.deaths = bankOwner.getInt("deaths");
        }
        return dbPlayer;
    }

    public Bank getBank(String bank_name) throws SQLException {
        ResultSet bank = query(
                "SELECT b.*, (SELECT SUM(balance) from bank_account where bank_name=b.bank_name) as customers_balance from bank as b where b.bank_name=?",
                bank_name);
        if (!bank.next())
            throw new SQLException("Bank not found");
        return serializeBank(bank);
    }

    public List<Bank> getBanks() throws SQLException {
        ResultSet banks = query(
                "SELECT b.*, (SELECT SUM(balance) from bank_account where bank_name=b.bank_name) as customers_balance from bank as b");
        return serializeBanks(banks);
    }

    public Bank getBankByOwner(String owner) throws SQLException {
        ResultSet bank = query(
                "SELECT b.*, (SELECT SUM(balance) from bank_account where bank_name=b.bank_name) as customers_balance from bank as b where b.owner=?",
                owner);
        if (!bank.next())
            throw new SQLException("Bank not found");
        return serializeBank(bank);
    }

    public Bank getBankByOwnerPlayer(Player player) throws SQLException {
        return this.getBankByOwner(player.getUniqueId().toString());
    }

    public PlayerBankAccount serializeBankAccount(ResultSet rs) throws SQLException {

        PlayerBankAccount bankAccount = new PlayerBankAccount();
        bankAccount.bank_name = rs.getString("bank_name");
        bankAccount.player_id = rs.getString("player_id");
        bankAccount.balance = rs.getFloat("balance");
        bankAccount.player_name = rs.getString("player_name");
        return bankAccount;
    }

    public List<PlayerBankAccount> getBankAccounts(String bank_name) throws SQLException {
        ResultSet bankAccounts = query(
                "SELECT ba.*, p.player_name from bank_account as ba inner join player as p on p.uid=ba.player_id where ba.bank_name=?",
                bank_name);
        // for each bank account
        List<PlayerBankAccount> bankAccountsList = new ArrayList<PlayerBankAccount>();
        while (bankAccounts.next()) {
            bankAccountsList.add(serializeBankAccount(bankAccounts));
        }
        return bankAccountsList;
    }
}
