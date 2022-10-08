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

        bankInventory.setItem(0, bankBalanceItem);
        bankInventory.setItem(1, viewLoansItem);
        bankInventory.setItem(2, depositItem);
        bankInventory.setItem(8, bankNameItem);

    }

    public void bankDeposit(Player player) throws SQLException, Error {

        /* Get bank account */
        PlayerBankAccount playerBankAccount = bankHelper.getPlayerBankAccount(player);

        Balance balance = balanceHelper.getBalanceFromPlayer(player);

        bankInventory = Bukkit.createInventory(null, rowsToSize(1), GUITitles.get("bank_deposit"));

        initializeBankDepositGUIItems(playerBankAccount, balance);
        player.openInventory(bankInventory);
    }

    public void initializeBankDepositGUIItems(PlayerBankAccount playerBankAccount, Balance playerBalance) {
        // Gold bar item for the bank balance and player balance, same item, slot 8
        ItemStack bankBalanceItem = this.createGuiItem(Material.DIAMOND, "§aYour bank balance",
                "balance",
                "§r§6" + NationsPlusEconomy.dollarFormat.format(playerBankAccount.balance));
        ItemStack playerBalanceItem = this.createGuiItem(Material.GOLD_INGOT, "§aYour wallet balance",
                "balance",
                "§r§6" + NationsPlusEconomy.dollarFormat.format(playerBalance.balance));
        bankInventory.setItem(8, bankBalanceItem);
        bankInventory.setItem(7, playerBalanceItem);

        // Paper with text "Deposit 100"
        ItemStack deposit100Item = this.createGuiItem(Material.PAPER, "§6Deposit §r§a$100", "deposit_100",
                "§f> §7Click to deposit §a$100");
        bankInventory.setItem(0, deposit100Item);

        // Paper with text "Deposit 250"
        ItemStack deposit250Item = this.createGuiItem(Material.PAPER, "§6Deposit §r§a$250", "deposit_250",
                "§f> §7Click to deposit §a$250");
        bankInventory.setItem(1, deposit250Item);

        // Paper with text "Deposit 500"

        ItemStack deposit500Item = this.createGuiItem(Material.PAPER, "§6Deposit §r§a$500", "deposit_500",
                "§f> §7Click to deposit §a$500");
        bankInventory.setItem(2, deposit500Item);

        // Paper with text "Deposit 1000"

        ItemStack deposit1000Item = this.createGuiItem(Material.PAPER, "§6Deposit §r§a$1000", "deposit_1000",
                "§f> §7Click to deposit §a$1000");

        bankInventory.setItem(3, deposit1000Item);

        // Paper with text "Deposit 2500"

        ItemStack deposit2500Item = this.createGuiItem(Material.PAPER, "§6Deposit §r§a$2500", "deposit_2500",
                "§f> §7Click to deposit §a$2500");

        bankInventory.setItem(4, deposit2500Item);

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

                if (identifier.equals("deposit")) {
                    player.closeInventory();
                    this.bankDeposit((Player) e.getWhoClicked());
                } else if (identifier.equals("view_loans")) {
                    player.performCommand("loans");
                    player.closeInventory();
                }
            } else if (title.equals(GUITitles.get("bank_deposit"))) {
                e.setCancelled(true);
                final ItemStack clickedItem = e.getCurrentItem();
                // verify current item is not null
                if (clickedItem == null || clickedItem.getType() == Material.AIR)
                    return;

                // get the identifier of the item, last line of lore
                String identifier = this.getIdentifier(clickedItem);
                if (!identifier.startsWith("deposit_"))
                    return;

                // make the player send /loans command
                player.performCommand("bank deposit " + identifier.split("_")[1]);
                player.closeInventory();
            }
        } catch (Error error) {
            handleError(player, error);
        } catch (SQLException error) {
            handleError(player, new Error("An error occurred while trying to access the database"));
            error.printStackTrace();
        }
    }

}
