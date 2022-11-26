package com.ollethunberg.commands.auction;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.lib.PermissionException;
import com.ollethunberg.lib.helpers.PlayerHelper;
import com.ollethunberg.lib.models.db.DBAuctionItem;
import com.ollethunberg.lib.models.db.DBPlayer;
import com.ollethunberg.utils.EnchantmentHelper;
import com.ollethunberg.utils.WalletBalanceHelper;

public class Auction extends WalletBalanceHelper {

    AuctionHelper auctionHelper = new AuctionHelper();
    PlayerHelper playerHelper = new PlayerHelper();
    public NationsPlusEconomy plugin = NationsPlusEconomy.getPlugin(NationsPlusEconomy.class);

    public void bid(Player player, Integer amount) throws SQLException, Error {
        if (amount <= 0) {
            throw new Error("You can't bid a negative amount");
        }
        DBAuctionItem activeAuctionItem = auctionHelper.getActiveAuctionItem();
        if (activeAuctionItem == null || plugin.auctionTask == null || plugin.auctionTask.isCancelled()) {
            throw new Error("No active auction item");
        }
        if (activeAuctionItem.highest_bid >= amount) {
            throw new Error("Bid must be higher than the current highest bid");
        }
        if (getBalance(player.getUniqueId().toString(), true) < amount) {
            throw new Error("You don't have enough money to bid that much");
        }

        auctionHelper.placeBid(activeAuctionItem.id, player.getUniqueId().toString(), amount);
        // get the player account which currently has the highest bid
        DBPlayer highestBidder = playerHelper.getPlayer(activeAuctionItem.highest_bidder_id);
        if (highestBidder != null) {
            // refund the previous highest bidder
            addBalancePlayer(highestBidder.uid, activeAuctionItem.highest_bid);
        }

        addBalancePlayer(player.getUniqueId().toString(), -amount);

        if (!plugin.auctionTask.isCancelled()) {
            if (plugin.auctionTimeLeft < 20) {
                plugin.auctionTimeLeft += 10;
                Bukkit.broadcastMessage(NationsPlusEconomy.auctionPrefix + "§aExtended auction time by 10 seconds");
            }

        }

        player.sendMessage(NationsPlusEconomy.auctionPrefix + "§aYou have successfully placed a bid of "
                + NationsPlusEconomy.dollarFormat.format(amount)
                + " on the current auction item");

        Bukkit.broadcastMessage(NationsPlusEconomy.auctionPrefix + "§a" + player.getDisplayName()
                + "§a has placed a bid of "
                + NationsPlusEconomy.dollarFormat.format(amount) + " on auction item: x" + activeAuctionItem.amount
                + " "
                + Material.getMaterial(activeAuctionItem.material).toString());

    }

    public void auction(Player player) throws SQLException, PermissionException {
        if (!player.hasPermission("npe.auction.create")) {
            throw new PermissionException("");
        }
        // get the item in the players main hand
        ItemStack item = player.getInventory().getItemInMainHand();
        // check if the item is null
        if (item == null || item.getType().isAir()) {
            throw new Error("You must be holding an item to auction it");
        }
        // check if the item is already in an auction
        DBAuctionItem currentAuctionItem = auctionHelper.getActiveAuctionItem();
        if (currentAuctionItem != null) {
            throw new Error("There is already an active auction item");
        }
        // create a new auction item
        DBAuctionItem auctionItem = new DBAuctionItem();
        auctionItem.amount = item.getAmount();
        auctionItem.lore_name = item.getItemMeta().getDisplayName();
        auctionItem.material = item.getType().toString();
        auctionItem.enchantments = EnchantmentHelper.getEnchantmentsAsString(item);
        auctionItem.highest_bid = 0;

        auctionHelper.createAuctionItem(auctionItem);
        Bukkit.broadcastMessage(
                NationsPlusEconomy.auctionPrefix + "§aNew auction item has been created: x" + item.getAmount() + " "
                        + item.getType() + "§a!");
        Bukkit.broadcastMessage(NationsPlusEconomy.auctionPrefix + "§aThe auction will end in 5 minutes");
        plugin.initAuctionTimer();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

    }

    public void endAuction() throws SQLException {
        DBAuctionItem item = auctionHelper.getActiveAuctionItem();
        if (item == null) {
            throw new Error("No auction item found");
        }
        if (item.highest_bidder_id == null) {
            throw new Error("No one has bid on the auction item");
        }

        Player auctionWinner = Bukkit.getPlayer(UUID.fromString((item.highest_bidder_id)));
        if (auctionWinner == null) {
            throw new Error("No auction winner found");
        }
        ItemStack auctionItem = new ItemStack(Material.getMaterial(item.material), item.amount);
        ItemMeta meta = auctionItem.getItemMeta();
        meta.setDisplayName(item.lore_name);
        if (item.enchantments != null) {
            auctionItem = EnchantmentHelper.addEnchants(auctionItem, item.enchantments);
        }
        auctionWinner.getInventory().addItem(auctionItem);
        auctionHelper.endAuction(item);
        Bukkit.broadcastMessage(NationsPlusEconomy.auctionPrefix + "§a" + auctionWinner.getDisplayName()
                + "§a has won the auction with a bid of "
                + NationsPlusEconomy.dollarFormat.format(item.highest_bid));
    }

}
