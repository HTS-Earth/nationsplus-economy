package com.ollethunberg.lib.helpers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ollethunberg.lib.models.Nation;
import com.ollethunberg.lib.models.db.DBNation;
import com.ollethunberg.lib.models.db.DBPlayer;

public class NationHelper extends SQLHelper {
    PlayerHelper playerHelper = new PlayerHelper();

    public DBNation serializeDBNation(ResultSet rs) throws SQLException {
        DBNation nation = new Nation();

        nation.name = rs.getString("name");
        nation.king_id = rs.getString("king_id");
        nation.successor_id = rs.getString("successor_id");
        nation.balance = rs.getFloat("balance");
        nation.kills = rs.getInt("kills");
        nation.transfer_tax = rs.getInt("transfer_tax");
        nation.income_tax = rs.getInt("income_tax");
        nation.market_tax = rs.getInt("market_tax");
        nation.vat_tax = rs.getInt("vat_tax");
        return nation;
    }

    public Nation serializeNation(ResultSet rs) throws SQLException {
        Nation nation = (Nation) serializeDBNation(rs);
        System.out.println(nation);
        nation.king_name = rs.getString("king_name");
        nation.membersCount = rs.getInt("membersCount");

        return nation;
    }

    public Nation getNation(String nationName) throws SQLException {

        ResultSet rs = query(
                "SELECT n.*, p.player_name as king_name, (select count(p.*)from player as p where p.nation = n.name) as \"membersCount\" from nation as n inner join player as p on p.uid=n.king_id where n.name=?",
                nationName);
        if (!rs.next()) {
            return null;
        }
        return serializeNation(rs);

    }

    public Nation getNationOfPlayer(String player_id) throws SQLException {
        DBPlayer dbPlayer = playerHelper.getPlayer(player_id);
        if (dbPlayer == null || dbPlayer.nation == null) {
            return null;
        }
        return getNation(dbPlayer.nation);
    }

    public void setTax(String nationName, String taxType, float tax) throws SQLException {
        switch (taxType) {
            case "income":
                update("UPDATE nation SET income_tax=? WHERE name=?", tax, nationName);
                break;
            case "transfer":
                update("UPDATE nation SET transfer_tax=? WHERE name=?", tax, nationName);
                break;
            case "market":
                update("UPDATE nation SET market_tax=? WHERE name=?", tax, nationName);
                break;
            default:
                throw new Error("Invalid tax type");
        }

    }

    public void addMoney(String nationName, float amount) throws SQLException {
        update("UPDATE nation SET balance=balance+? WHERE name=?", amount, nationName);
    }
}
