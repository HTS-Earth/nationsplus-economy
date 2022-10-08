package com.ollethunberg.commands.bank;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.GUI.GUIManager;
import com.ollethunberg.commands.bank.classes.PlayerBankAccount;
import com.ollethunberg.commands.bank.classes.PlayerBankInfo;
import com.ollethunberg.database.DBPlayer;
import com.ollethunberg.lib.ColorHelper;

public class BankGUI extends GUIManager implements Listener {
    private Inventory bankInventory;
    private BankHelper bankHelper;

    public BankGUI() {

        /* Register */
        GUITitles.put("bank", "§2§l$$ §r§a§lBank §r§2§l$$");
        /* Initialize BankHelper */
        bankHelper = new BankHelper();
    }

    public void bankAccount(Player player) throws SQLException, Error {
        /** Get bank information */
        PlayerBankInfo playerBankInfo = bankHelper.getPlayerBankInfo(player);
        /* Get bank account */
        PlayerBankAccount playerBankAccount = bankHelper.getPlayerBankAccount(player);
        /* Get bank owner */
        DBPlayer bankOwner = bankHelper.getBankOwner(playerBankInfo.bank_name);

        bankInventory = Bukkit.createInventory(null, rowsToSize(1), GUITitles.get("bank"));
        this.initializeBankGUIItems(playerBankInfo, playerBankAccount, bankOwner);
        player.openInventory(bankInventory);
    }

    private void initializeBankGUIItems(PlayerBankInfo playerBankInfo, PlayerBankAccount playerBankAccount,
            DBPlayer bankOwner) {

        // Bank name
        ItemStack bankNameItem = this.createGuiItem(Material.NAME_TAG, "§r§l§a" + playerBankInfo.bank_name, "bank_name",
                "§7Saving interest: §r§l§a" + playerBankInfo.saving_interest * 100 + "%",
                "§7Bank funds: §r§l§a" + NationsPlusEconomy.dollarFormat.format(playerBankAccount.balance) + "§r§l§a",
                "§7Bank safety rating: §r§l§a" + ColorHelper.addColorToPercentage(playerBankInfo.safety_rating),
                "§7Bank owner: §r§l§a" + bankOwner.player_name,
                "§7Bank based in: §r§l§a" + bankOwner.nation);

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
