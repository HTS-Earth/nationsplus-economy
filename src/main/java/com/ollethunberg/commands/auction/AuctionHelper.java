package com.ollethunberg.commands.auction;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.lib.helpers.SQLHelper;
import com.ollethunberg.lib.models.db.DBAuctionItem;

public class AuctionHelper extends SQLHelper {

    public void clearAuction() {
        try {
            update("DELETE FROM auction WHERE date_sold IS NULL");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DBAuctionItem serializeAuctionItem(ResultSet rs) throws SQLException {

        DBAuctionItem auctionItem = new DBAuctionItem();
        auctionItem.material = rs.getString("material");
        auctionItem.amount = rs.getInt("amount");
        auctionItem.lore_name = rs.getString("lore_name");
        auctionItem.id = rs.getInt("id");
        auctionItem.highest_bid = rs.getInt("highest_bid");
        auctionItem.enchantments = rs.getString("enchantments");

        auctionItem.highest_bidder_id = rs.getString("highest_bidder_id");
        auctionItem.date_sold = rs.getString("date_sold");
        return auctionItem;

    }

    public DBAuctionItem getActiveAuctionItem() throws SQLException {
        ResultSet item = query("select * from auction where date_sold is null");
        if (!item.next()) {
            NationsPlusEconomy.LOGGER.info("auction doesnt exist");
            return null;
        }
        return serializeAuctionItem(item);

    }

    public void createAuctionItem(DBAuctionItem item) throws SQLException, Error {
        update("INSERT INTO auction (amount, lore_name, material, highest_bid, enchantments, highest_bidder_id, date_sold) VALUES (?, ?,?, ?, NULL, ?, NULL);",
                item.amount, item.lore_name, item.material, 0,
                item.enchantments);

    }

    public void placeBid(Integer auctionItemId, String bidder_id, Integer bid) throws SQLException {
        update("update auction set highest_bid = ?, highest_bidder_id = ? where id = ?", bid, bidder_id, auctionItemId);
    }

    public void endAuction(DBAuctionItem item) throws SQLException {
        update("update auction set date_sold = now() where id = ?", item.id);
    }

}
