package com.ollethunberg.commands.bank;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ollethunberg.NationsPlusEconomy;
import com.ollethunberg.GUI.GUIManager;
import com.ollethunberg.commands.balance.BalanceHelper;
import com.ollethunberg.commands.balance.models.Balance;
import com.ollethunberg.commands.bank.models.PlayerBankAccount;
import com.ollethunberg.commands.bank.models.PlayerBankInfo;
import com.ollethunberg.database.DBPlayer;
import com.ollethunberg.lib.ColorHelper;

public class BankGUI extends GUIManager implements Listener {
    private Inventory bankInventory;
    private BankHelper bankHelper;
    private BalanceHelper balanceHelper;

    public BankGUI() {

        /* Register */
        GUITitles.put("bank", "§2Bank");
        GUITitles.put("bank_deposit",
                "§2Amount to deposit");
        GUITitles.put("bank_withdraw",
                "§2Amount to withdraw");
        /* Initialize BankHelper */
        bankHelper = new BankHelper();
        balanceHelper = new BalanceHelper();
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
        ItemStack bankBalanceItem = this.createGuiItem(Material.GOLD_INGOT, "§6Your bank balance",
                "balance",
                "§r§a" + NationsPlusEconomy.dollarFormat.format(playerBankAccount.balance));

        // paper with text "View Loans"
        ItemStack viewLoansItem = this.createGuiItem(Material.PAPER, "§6View Loans", "view_loans",
                "§f> §7Click to view your loans");

        // chest with text "Deposit"
        ItemStack depositItem = this.createGuiItem(Material.CHEST, "§6Deposit money", "deposit",
                "§f> §7Click to deposit money");
        // chest with text "Withdraw"
        ItemStack withdrawItem = this.createGuiItem(Material.CHEST, "§6Withdraw money", "withdraw",
                "§f> §7Click to withdraw money");

        bankInventory.setItem(7, bankBalanceItem);
        bankInventory.setItem(0, viewLoansItem);
        bankInventory.setItem(1, depositItem);
        bankInventory.setItem(2, withdrawItem);
        bankInventory.setItem(8, bankNameItem);

    }

    public void bankDepositOrWithdraw(Player player, String action) throws SQLException, Error {

        /* Get bank account */
        PlayerBankAccount playerBankAccount = bankHelper.getPlayerBankAccount(player);

        Balance balance = balanceHelper.getBalanceFromPlayer(player);

        bankInventory = Bukkit.createInventory(null, rowsToSize(1),
                action.equals("deposit") ? GUITitles.get("bank_deposit") : GUITitles.get("bank_withdraw"));

        initializeBankDepositOrWithdrawGUIItems(playerBankAccount, balance, action);
        player.openInventory(bankInventory);
    }

    public void initializeBankDepositOrWithdrawGUIItems(PlayerBankAccount playerBankAccount, Balance playerBalance,
            String action) {
        if (!action.equals("deposit") && !action.equals("withdraw")) {
            throw new Error("Action must be either deposit or withdraw");
        }
        String actionCapitalized = action.equals("deposit") ? "Deposit" : "Withdraw";
        // Gold bar item for the bank balance and player balance, same item, slot 8
        ItemStack bankBalanceItem = this.createGuiItem(Material.DIAMOND, "§aYour bank balance",
                "balance",
                "§r§6" + NationsPlusEconomy.dollarFormat.format(playerBankAccount.balance));
        ItemStack playerBalanceItem = this.createGuiItem(Material.GOLD_INGOT, "§aYour wallet balance",
                "balance",
                "§r§6" + NationsPlusEconomy.dollarFormat.format(playerBalance.balance));
        bankInventory.setItem(8, bankBalanceItem);
        bankInventory.setItem(7, playerBalanceItem);

        // Paper with text "<Action> 100"
        ItemStack item100 = this.createGuiItem(Material.PAPER, "§6" + actionCapitalized + " §r§a$100", action + "_100",
                "§f> §7Click to " + action + " §a$100");
        bankInventory.setItem(0, item100);

        // Paper with text "<Action> 250"
        ItemStack item250 = this.createGuiItem(Material.PAPER, "§6" + actionCapitalized + " §r§a$250", action + "_250",
                "§f> §7Click to " + action + " §a$250");

        bankInventory.setItem(1, item250);

        // Paper with text "<Action> 500"
        ItemStack item500 = this.createGuiItem(Material.PAPER, "§6" + actionCapitalized + " §r§a$500", action + "_500",
                "§f> §7Click to " + action + " §a$500");

        bankInventory.setItem(2, item500);

        // Paper with text "<Action> 1000"

        ItemStack item1000 = this.createGuiItem(Material.PAPER, "§6" + actionCapitalized + " §r§a$1000",
                action + "_1000",
                "§f> §7Click to " + action + " §a$1000");

        bankInventory.setItem(3, item1000);

        // Paper with text "<Action> 2500"
        ItemStack item2500 = this.createGuiItem(Material.PAPER, "§6" + actionCapitalized + " §r§a$2500",
                action + "_2500",
                "§f> §7Click to " + action + " §a$2500");

        bankInventory.setItem(4, item2500);

    }

    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {
        String title = e.getView().getTitle();
        // get the player
        Player player = (Player) e.getWhoClicked();
        try {

            if (title.equals(GUITitles.get("bank"))) {
                e.setCancelled(true);
                final ItemStack clickedItem = e.getCurrentItem();
                // verify current item is not null
                if (clickedItem == null || clickedItem.getType() == Material.AIR)
                    return;

                // get the identifier of the item, last line of lore
                String identifier = this.getIdentifier(clickedItem);

                if (identifier.equals("deposit") || identifier.equals("withdraw")) {
                    player.closeInventory();
                    this.bankDepositOrWithdraw((Player) e.getWhoClicked(), identifier);
                } else if (identifier.equals("view_loans")) {
                    player.performCommand("loans");
                    player.closeInventory();
                }
            } else if (title.equals(GUITitles.get("bank_deposit")) || title.equals(GUITitles.get("bank_withdraw"))) {
                e.setCancelled(true);
                final ItemStack clickedItem = e.getCurrentItem();
                // verify current item is not null
                if (clickedItem == null || clickedItem.getType() == Material.AIR)
                    return;

                // get the identifier of the item, last line of lore
                String identifier = this.getIdentifier(clickedItem);
                if (!identifier.startsWith("deposit_") && !identifier.startsWith("withdraw_"))
                    return;

                // make the player send /loans command
                player.performCommand((identifier.startsWith("deposit_") ? "bank deposit " : "bank withdraw ")
                        + identifier.split("_")[1]);
                // TODO: UPDATE GUI ITEMS for the balance and player balance

            }
        } catch (Error error) {
            handleError(player, error);
        } catch (SQLException error) {
            handleError(player, new Error("An error occurred while trying to access the database"));
            error.printStackTrace();
        }
    }

}
