package com.ollethunberg.commands.market;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ollethunberg.lib.helpers.SQLHelper;
import com.ollethunberg.lib.models.db.DBMarketListing;
import com.ollethunberg.utils.EnchantmentHelper;

public class MarketHelper extends SQLHelper {
    public DBMarketListing serializeMarketListing(ResultSet rs) throws SQLException {
        DBMarketListing listing = new DBMarketListing();

        listing.seller_id = rs.getString("seller_id");
        listing.buyer_id = rs.getString("buyer_id");
        listing.id = rs.getInt("id");
        listing.material = rs.getString("material");
        listing.amount = rs.getInt("amount");
        listing.lore_name = rs.getString("lore_name");
        listing.lore = rs.getString("lore");
        listing.price = rs.getInt("price");
        listing.enchantments = rs.getString("enchantments");
        listing.date = rs.getString("date");
        listing.date_sold = rs.getString("date_sold");

        return listing;
    }

    public List<DBMarketListing> serializeMarketListings(ResultSet rs) throws Exception {
        List<DBMarketListing> listings = new ArrayList<DBMarketListing>();
        while (rs.next()) {
            listings.add(serializeMarketListing(rs));
        }
        return listings;
    }

    public DBMarketListing getMarketListing(Integer id) throws Error, SQLException {
        String query = "SELECT * FROM market_listing WHERE id = ?";
        ResultSet rs = query(query, id);
        if (rs.next()) {
            return serializeMarketListing(rs);
        } else {
            throw new Error("No listing found");
        }

    }

    enum ListingStatus {
        ALL, SOLD, UNSOLD
    }

    public List<DBMarketListing> getMarketListings(ListingStatus status, Integer page) throws SQLException, Exception {
        // if executed is false, then only return listings with no buyer_id and no
        // date_sold
        switch (status) {
            case ALL:
                return serializeMarketListings(query("SELECT * FROM market_listing LIMIT 45 OFFSET ?", page * 45));
            case SOLD:
                return serializeMarketListings(
                        query("SELECT * FROM market_listing WHERE buyer_id IS NOT NULL AND date_sold IS NOT NULL LIMIT 45 OFFSET ?",
                                page * 45));
            case UNSOLD:
                return serializeMarketListings(
                        query("SELECT * FROM market_listing WHERE buyer_id IS NULL AND date_sold IS NULL LIMIT 45 OFFSET ?",
                                page * 45));
            default:
                return serializeMarketListings(query("SELECT * FROM market_listing LIMIT 45 OFFSET ?", page * 45));
        }

    }

    public List<DBMarketListing> getMarketListingsByPlayerId(String playerId, Integer page)
            throws SQLException, Exception {
        ResultSet rs = query("SELECT * from market_listing WHERE seller_id = ? LIMIT 45 OFFSET ?", playerId, page * 45);
        // serialize market listings
        return serializeMarketListings(rs);
    }

    public void addMarketListing(DBMarketListing listing) throws SQLException, Exception {
        String query = "INSERT INTO market_listing (seller_id, material, amount, lore_name, price, enchantments, date, lore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        update(query, listing.seller_id, listing.material, listing.amount.toString(), listing.lore_name,
                listing.price.toString(), listing.enchantments, listing.date, listing.lore);
    }

    public ItemStack createItemFromListing(DBMarketListing listing) throws Error {
        ItemStack item = new ItemStack(Material.getMaterial(listing.material), listing.amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(listing.lore_name);
        // set enchantments
        if (listing.enchantments != null) {
            item = EnchantmentHelper.addEnchants(item, listing.enchantments);
        }
        // add lore
        if (listing.lore != null) {
            List<String> lore = Arrays.asList(listing.lore.split(","));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);

        return item;
    }

    // remove market listing by id
    public void deleteMarketListing(Integer id) throws SQLException, Error {
        String query = "DELETE FROM market_listing WHERE id = ?";
        update(query, id);
    }

    public void executeMarketListing(String buyer_id, Integer id) throws SQLException, Error {
        String query = "UPDATE market_listing SET buyer_id = ?, date_sold = ? WHERE id = ?";
        update(query, buyer_id, new java.util.Date().toString(), id);
    }

}
