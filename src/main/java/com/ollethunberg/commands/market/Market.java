package com.ollethunberg.commands.market;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.lib.helpers.NationHelper;
import com.ollethunberg.lib.helpers.PlayerHelper;
import com.ollethunberg.lib.models.Nation;
import com.ollethunberg.lib.models.db.DBMarketListing;
import com.ollethunberg.lib.models.db.DBPlayer;
import com.ollethunberg.utils.EnchantmentHelper;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Market extends WalletBalanceHelper {

    MarketHelper marketHelper = new MarketHelper();
    NationHelper nationHelper = new NationHelper();
    PlayerHelper playerHelper = new PlayerHelper();

    public void addMarketListing(Player player, Integer price) throws SQLException, Exception {
        ItemStack item = player.getInventory().getItemInMainHand();
        // check the item has been used
        if (item == null || item.getType().isAir())
            throw new Exception("§cYou need to hold an item in your hand to sell it!");

        if (price <= 0)
            throw new Exception("Price must be greater than 0");

        // check if the item has been damaged
        Damageable damageable = (Damageable) item.getItemMeta();

        if (damageable.getDamage() > 0)
            throw new Exception("§cYou can't sell damaged items!");

        if (price > 10000000)
            throw new Exception("§cYou can't sell items for more than 10,000,000!");
        // get listing
        DBMarketListing listing = new DBMarketListing();
        listing.seller_id = player.getUniqueId().toString();
        listing.material = item.getType().toString();
        listing.amount = item.getAmount();
        listing.lore_name = item.getItemMeta().getDisplayName();
        listing.price = price;

        listing.enchantments = EnchantmentHelper.getEnchantmentsAsString(item);

        listing.date = new java.util.Date().toString();
        if (item.getItemMeta().hasLore()) {
            listing.lore = String.join(",", item.getItemMeta().getLore());
        }

        player.getInventory().removeItem(item);

        marketHelper.addMarketListing(listing);
        player.sendMessage(
                NationsPlusEconomy.marketPrefix + "§aAdded market listing! View your listings with /listings");
        Bukkit.broadcastMessage(NationsPlusEconomy.marketPrefix + "§7" + player.getName() + ": new market listing §a"
                + NationsPlusEconomy.dollarFormat.format(price) + "§7 for §6" + item.getAmount() + "§7x §6"
                + item.getType().name() + ". View by /market");

    }

    public void buyMarketListing(Player player, Integer id) throws SQLException, Error {
        // get the listing from database
        DBMarketListing listing = marketHelper.getMarketListing(id);
        if (listing.seller_id.equals(player.getUniqueId().toString()))
            throw new Error("You can't buy your own listing!");

        // check if the player has enough money
        DBPlayer dbPlayer = playerHelper.getPlayer(player.getUniqueId().toString());
        if (dbPlayer == null)
            throw new Error("You don't have a wallet!");

        // get the VAT from the buyers nation
        Nation buyerNation = nationHelper.getNation(dbPlayer.nation);
        // log the vat
        NationsPlusEconomy.LOGGER.info("VAT: " + buyerNation.vat_tax);
        float buyerVATAmount = buyerNation != null ? (listing.price * (buyerNation.vat_tax / 100f)) : 0f;
        if (dbPlayer.balance < (listing.price + buyerVATAmount))
            throw new Error("§cYou don't have enough money to buy this item!");

        // check if the player has enough space in inventory
        if (player.getInventory().firstEmpty() == -1)
            throw new Error("§cYou don't have enough space in your inventory to buy this item!");

        // create the item
        ItemStack item = marketHelper.createItemFromListing(listing);

        DBPlayer dbSeller = playerHelper.getPlayer(listing.seller_id);
        if (dbSeller == null)
            throw new Error("Seller doesn't have a wallet!");
        // get market_tax from sellers nation
        Nation sellerNation = nationHelper.getNation(dbSeller.nation);
        float sellerNationMarketTax = sellerNation != null ? listing.price * sellerNation.market_tax / 100f : 0f;

        // calculate tax
        float tax = listing.price * (sellerNationMarketTax / 100f);

        // remove the money from the player
        addBalancePlayer(player.getUniqueId().toString(), -(listing.price + buyerVATAmount));

        addBalancePlayer(listing.seller_id, listing.price - tax);

        nationHelper.addMoney(dbSeller.nation, tax);
        if (buyerNation != null)
            nationHelper.addMoney(buyerNation.name, buyerVATAmount);

        // remove the listing from the database
        marketHelper.executeMarketListing(dbPlayer.uid, id);

        // add the item to the player
        player.getInventory().addItem(item);

        // send message to the player
        player.sendMessage(NationsPlusEconomy.marketPrefix +
                "§aYou bought " + listing.amount + " of " + listing.material + " for "
                + NationsPlusEconomy.dollarFormat.format(listing.price) + " and paid "
                + NationsPlusEconomy.dollarFormat.format(buyerVATAmount) + " in taxes");
        // send message to the seller if he is online
        Player seller = plugin.getServer().getPlayer(listing.seller_id);
        if (seller != null) {
            seller.sendMessage(NationsPlusEconomy.marketPrefix +
                    "§7Your item " + listing.material + " was sold for §a"
                    + NationsPlusEconomy.dollarFormat.format(listing.price) + "§7!",
                    "§7You earned §a" + NationsPlusEconomy.dollarFormat.format(listing.price - tax)
                            + "§7 after tax!");
        }
        Material material = Material.getMaterial(listing.material);
        Bukkit.broadcastMessage(NationsPlusEconomy.marketPrefix + "§7" + "x" + listing.amount + " " + material.name()
                + " was sold for §a"
                + NationsPlusEconomy.dollarFormat.format(listing.price) + "§7!");

    }

    public void deleteMarketListing(Player player, Integer id) throws Error, SQLException {
        // get the listing from database
        DBMarketListing listing = marketHelper.getMarketListing(id);
        if (!listing.seller_id.equals(player.getUniqueId().toString())) {
            throw new Error("You can't delete someone else's listing!");
        }
        // check if the listing is sold or has a buyer
        if (listing.buyer_id != null || listing.date_sold != null)
            throw new Error("You can't delete a listing that has been sold!");

        // remove the listing from the database
        marketHelper.deleteMarketListing(id);

        // check if player has inventory space
        if (player.getInventory().firstEmpty() == -1)
            throw new Error("§cYou don't have enough space in your inventory to get your item back!");

        // create the item
        ItemStack item = marketHelper.createItemFromListing(listing);
        player.getInventory().addItem(item);

        // send message to the player
        player.sendMessage("§aYou deleted your listing for " + listing.material + "!");

    }

}
