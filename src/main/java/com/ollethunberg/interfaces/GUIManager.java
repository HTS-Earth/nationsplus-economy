package com.ollethunberg.interfaces;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.commands.bank.BankHelper;
import com.ollethunberg.commands.bank.classes.PlayerBankAccount;
import com.ollethunberg.commands.bank.classes.PlayerBankInfo;
import com.ollethunberg.lib.ColorHelper;
import com.ollethunberg.utils.WalletBalanceHelper;

public class GUIManager extends WalletBalanceHelper implements Listener {

    private Inventory bankInventory;
    private BankHelper bankHelper;
    /*
     * bank: "§2§l$$ §r§a§lBank §r§2§l$$"
     * 
     */
    Map<String, String> GUITitles = new HashMap<String, String>();

    public GUIManager() {

        initializeGUITilesMap();

        /* Initialize helpers */
        bankHelper = new BankHelper();
    }

    private void initializeGUITilesMap() {
        GUITitles.put("bank", "§2§l$$ §r§a§lBank §r§2§l$$");
    }

    public void bankGUI(Player player) throws SQLException, Error {
        /** Get bank information */
        PlayerBankInfo playerBankInfo = bankHelper.getPlayerBankInfo(player);
        /* Get bank account */
        PlayerBankAccount playerBankAccount = bankHelper.getPlayerBankAccount(player);

        bankInventory = Bukkit.createInventory(null, rowsToSize(1), GUITitles.get("bank"));
        this.initializeBankGUIItems(playerBankInfo, playerBankAccount);
        player.openInventory(bankInventory);
    }

    private void initializeBankGUIItems(PlayerBankInfo playerBankInfo, PlayerBankAccount playerBankAccount) {

        // Bank name
        ItemStack bankNameItem = this.createGuiItem(Material.NAME_TAG, "§r§l§a" + playerBankInfo.bank_name, "bank_name",
                "§7Saving interest: §r§l§a" + playerBankInfo.saving_interest * 100 + "%",
                "§7Bank funds: §r§l§a" + NationsPlusEconomy.dollarFormat.format(playerBankAccount.balance) + "§r§l§a",
                "§7Bank safety rating: §r§l§a" + ColorHelper.addColorToPercentage(playerBankInfo.safety_rating));

        // Gold bar item for the bank balance
        ItemStack bankBalanceItem = this.createGuiItem(Material.GOLD_INGOT, "§aYour bank balance",
                "balance",
                "§r§6" + NationsPlusEconomy.dollarFormat.format(playerBankAccount.balance));

        // paper with text "View Loans"
        ItemStack viewLoansItem = this.createGuiItem(Material.PAPER, "§r§l§aView Loans", "view_loans",
                "§7Click to view your loans");

        bankInventory.setItem(0, bankNameItem);
        bankInventory.setItem(1, bankBalanceItem);
        bankInventory.setItem(2, viewLoansItem);

    }

    protected ItemStack createGuiItem(final Material material, final String name, String identifier,
            final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        List<String> list = new LinkedList<>(Arrays.asList(lore));

        list.add(convertToInvisibleString(identifier));

        // Set the lore of the item
        meta.setLore(list);

        item.setItemMeta(meta);

        return item;
    }

    private static int rowsToSize(int rows) {
        return rows * 9;
    }

    private static String convertToInvisibleString(String s) {
        System.out.println("GUIManager.convertToInvisibleString() " + s);
        String hidden = "";

        for (char c : s.toCharArray())
            hidden += ChatColor.COLOR_CHAR + "" + c;
        return hidden;
    }

    private static String convertFromInvisibleString(String s) {
        return s.replaceAll("(?i)" + ChatColor.COLOR_CHAR, "");
    }

    @EventHandler()
    public void onInventoryClick(final InventoryDragEvent e) {

        String title = e.getView().getTitle();
        //
        if (title.equals("§2§l$$ §r§a§lBank §r§2§l$$")) {
            e.setCancelled(true);
        }
    }

    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {

        String title = e.getView().getTitle();

        if (title.equals(GUITitles.get("bank"))) {
            e.setCancelled(true);
            final ItemStack clickedItem = e.getCurrentItem();
            // verify current item is not null
            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;
            // get the identifier of the item
            String identifier = convertFromInvisibleString(clickedItem.getItemMeta().getLore().get(1));
            // get the player
            Player player = (Player) e.getWhoClicked();
            // check the identifier
            switch (identifier) {
                case "view_loans":
                    player.sendMessage(identifier);
                    break;
                default:
                    break;
            }

        }
    }

}
