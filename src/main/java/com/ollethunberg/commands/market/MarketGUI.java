package com.ollethunberg.commands.market;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ollethunberg.GUI.GUIManager;
import com.ollethunberg.lib.models.db.DBMarketListing;

import net.md_5.bungee.api.ChatColor;

public class MarketGUI extends GUIManager implements Listener {

    Market market = new Market();
    MarketHelper marketHelper = new MarketHelper();
    private Inventory inventory;
    private static Locale usa = new Locale("en", "US");

    public static NumberFormat dollarFormat = NumberFormat.getCurrencyInstance(usa);

    public MarketGUI() {
        GUITitles.put("market", "§2Global market listings");
    }

    public void openMarketGUI(Player player) throws SQLException, Exception {
        // get listing
        List<DBMarketListing> listings = marketHelper.getMarketListings();
        openListingsGUIItems(player, listings);
    }

    private ItemStack getListingGUIItem(DBMarketListing listing, String... additionalLore) {
        Material m = Material.getMaterial(listing.material);
        ItemStack item = new ItemStack(m, listing.amount);
        // add enchants
        for (String enchant : listing.enchantments.split(",")) {
            if (enchant.equals(""))
                continue;
            String[] enchantSplit = enchant.split(":");
            item.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantSplit[0])),
                    Integer.parseInt(enchantSplit[1]));
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(listing.lore_name);

        ArrayList<String> lore = new ArrayList<String>();

        lore.add(ChatColor.WHITE + "Price: "
                + "§a" + dollarFormat.format(listing.price));
        lore.add(ChatColor.WHITE + "Seller: "
                + "§a" + Bukkit.getOfflinePlayer(UUID.fromString(listing.seller_id)).getName());

        lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Right click" + ChatColor.WHITE + " to "
                + ChatColor.GREEN + "" + ChatColor.BOLD + "BUY" + ChatColor.WHITE + " this item");
        // add identifier
        lore.add(convertToInvisibleString("buy#" + listing.id));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public void openListingsGUIItems(Player player, List<DBMarketListing> listings) {
        inventory = Bukkit.createInventory(null, rowsToSize(6), GUITitles.get("market"));
        // for loop with index
        for (int i = 0; i < listings.size(); i++) {
            inventory.setItem(i, getListingGUIItem(listings.get(i)));
        }
        player.openInventory(inventory);

    }

    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {
        String title = e.getView().getTitle();
        // get the player
        Player player = (Player) e.getWhoClicked();
        try {

            if (title.equals(GUITitles.get("market"))) {
                e.setCancelled(true);
                final ItemStack clickedItem = e.getCurrentItem();
                String identifer = getIdentifier(clickedItem);
                if (identifer == null)
                    return;
                String[] identifierSplit = identifer.split("#");
                if (identifierSplit[0].equals("buy")) {
                    market.buyMarketListing(player, Integer.parseInt(identifierSplit[1]));
                    openMarketGUI(player);
                }
            }
        } catch (SQLException error) {
            handleError(player, new Error("An error occurred while trying to access the database"));
            error.printStackTrace();
        } catch (Error | Exception error) {
            if (error instanceof Exception) {
                handleError(player, new Error("An error occurred while trying to access the database"));
                error.printStackTrace();
            } else {
                handleError(player, (Error) error);
            }

        }
    }
}
