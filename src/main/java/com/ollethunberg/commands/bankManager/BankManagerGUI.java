package com.ollethunberg.commands.bankManager;

import java.util.List;

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
import com.ollethunberg.commands.bank.models.Bank;
import com.ollethunberg.commands.bank.models.PlayerBankAccount;
import com.ollethunberg.commands.loan.LoanHelper;
import com.ollethunberg.commands.loan.models.DBLoan;
import com.ollethunberg.lib.models.db.DBPlayer;
import com.ollethunberg.lib.ColorHelper;

public class BankManagerGUI extends GUIManager implements Listener {

    private Inventory inventory;

    private LoanHelper loanHelper = new LoanHelper();

    public BankManagerGUI() {
        /* Register */
        GUITitles.put("bankManager", "§2Bank Manager");
    }

    public void listLoans(Player player, List<DBLoan> loans, List<DBPlayer> players) throws Exception {
        inventory = Bukkit.createInventory(null, rowsToSize(4), GUITitles.get("bankManager"));
        this.initializeListLoansGUIItems(loans, players);
        player.openInventory(inventory);
    }

    private void initializeListLoansGUIItems(List<DBLoan> loans, List<DBPlayer> players) throws Exception {
        for (int i = 0; i < loans.size(); i++) {
            DBLoan loan = loans.get(i);
            String player_id = loan.player_id;
            String player_name = "";
            for (DBPlayer player : players) {
                if (player.uid.equals(player_id)) {
                    player_name = player.player_name;
                }
            }
            /* Create GUI item */
            ItemStack item = this.loanInfoItem(loan, player_name);

            /* Add item to inventory */
            inventory.setItem(i, item);
        }
        // Back button to bank manager
        inventory.setItem(inventory.getSize() - 1,
                this.createGuiItem(Material.BARRIER, "§c§lBack", "cmd#bm", "§7Go back to bank manager"));

    }

    private ItemStack loanInfoItem(DBLoan loan, String player_name) throws Exception {
        String paidToTotalRateColor = ColorHelper.getColorFromPercentage(loan.amount_paid / loan.amount_total);
        /* Create GUI item */
        ItemStack item = this.createGuiItem(loan.active ? Material.EMERALD_BLOCK : Material.NAME_TAG,
                (loan.active ? "§2§lACTIVE Loan #" : "§aLoan #") + loan.id,
                "cmd#" + encodeCmd(
                        "bm loans info " + loan.id) + "#" + encodeCmd("bm loans" + (loan.accepted ? "" : " offers")),
                "§7Interest rate: §r§l§a" + loan.interest_rate * 100 + "%",
                "§7Amount paid: §r" + paidToTotalRateColor
                        + NationsPlusEconomy.dollarFormat.format(loan.amount_paid) + "§f/§a"
                        + NationsPlusEconomy.dollarFormat.format(loan.amount_total),
                "§7Paid off percentage: " + paidToTotalRateColor
                        + (int) (loan.amount_paid / loan.amount_total * 100) + "%",
                "§7Payments: §r§l§a" + (loan.payments_total - loan.payments_left) + "§f/§a" + loan.payments_total,
                loan.active ? "§7Income per hour: §r§l§a"
                        + NationsPlusEconomy.dollarFormat.format(loanHelper.getCostPerHour(loan)) : "",
                "§7Player: §r§l§a" + player_name);
        return item;
    }

    public void loanInfo(Player player, DBLoan loan, String customerName, String backCmd) throws Exception {
        inventory = Bukkit.createInventory(null, rowsToSize(1), GUITitles.get("bankManager"));
        this.initializeLoanInfoGUIItems(player, loan, customerName, backCmd);
        player.openInventory(inventory);
    }

    private void initializeLoanInfoGUIItems(Player player, DBLoan loan, String customerName, String backCmd)
            throws Exception {
        // GUI Loan info item, with loan id
        // check if loan is active or not
        if (loan.accepted && loan.active) {
            // GUI item for cancel loan
            ItemStack item = this.createGuiItem(Material.RED_CONCRETE, "§c§lCancel Loan", "cancel_loan#" + loan.id,
                    "§7Click to cancel loan #" + loan.id);
            inventory.setItem(0, item);
        } else if (!loan.accepted && !loan.active) {
            // GUI item for accept loan offer
            ItemStack acceptItem = this.createGuiItem(Material.LIME_CONCRETE, "§a§lAccept Loan",
                    "accept_loan#" + loan.id,
                    "§7Click to accept loan #" + loan.id);
            inventory.setItem(0, acceptItem);
            // GUI ITEM for decline loan offer
            ItemStack declineItem = this.createGuiItem(Material.RED_CONCRETE, "§c§lDecline Loan",
                    "decline_loan#" + loan.id,
                    "§7Click to decline loan #" + loan.id);
            inventory.setItem(1, declineItem);
        } else {
            // GUI
        }
        // loan info item
        ItemStack loanInfoItem = this.loanInfoItem(loan, customerName);
        inventory.setItem(7, loanInfoItem);

        NationsPlusEconomy.LOGGER.info(backCmd);
        // go back
        ItemStack backItem = this.createGuiItem(Material.BARRIER, "§c§lGo back", "cmd#" + encodeCmd(backCmd),
                "§7Click to go back");
        inventory.setItem(8, backItem);

    }

    public void getAccounts(Player player, List<PlayerBankAccount> accounts) throws Exception {
        inventory = Bukkit.createInventory(null, rowsToSize(4), GUITitles.get("bankManager"));
        this.initializeGetAccountsGUIItems(accounts);
        player.openInventory(inventory);
    }

    private void initializeGetAccountsGUIItems(List<PlayerBankAccount> accounts) throws Exception {
        for (int i = 0; i < accounts.size(); i++) {
            PlayerBankAccount account = accounts.get(i);
            /* Create GUI item */
            ItemStack item = this.accountInfoItem(account);

            /* Add item to inventory */
            inventory.setItem(i, item);
        }
        // Back button to bank manager
        inventory.setItem(inventory.getSize() - 1,
                this.createGuiItem(Material.BARRIER, "§c§lBack", "cmd#bm", "§7Go back to bank manager"));

    }

    private ItemStack accountInfoItem(PlayerBankAccount account) throws Exception {

        /* Create GUI item */
        ItemStack item = this.createGuiItem(Material.NAME_TAG, "§aAccount of " + account.player_name,
                "",
                "§7Balance: §r" + NationsPlusEconomy.dollarFormat.format(account.balance),
                "§7Player: §r§l§a" + account.player_name);
        return item;
    }

    public void bankManager(Player player, Bank bank) throws Exception {
        inventory = Bukkit.createInventory(null, rowsToSize(1), GUITitles.get("bankManager"));
        this.initializeBankManagerGUIItems(player, bank);
        player.openInventory(inventory);
    }

    public void initializeBankManagerGUIItems(Player player, Bank bank) throws Exception {
        // GUI item for bank info
        ItemStack bankInfoItem = this.createGuiItem(Material.CHEST, "§a§lBank Info", "bank_info",
                "§7Interest rate: §r§l§a" + bank.saving_interest * 100 + "%",
                "§7Safety rating: §r§l§a" + ColorHelper.getColorFromPercentage(bank.safety_rating),
                "§7Bank balance: §r§l§a" + NationsPlusEconomy.dollarFormat.format(bank.balance),
                "§7Customers balance: §r§l§a" + NationsPlusEconomy.dollarFormat.format(bank.customers_balance));
        inventory.setItem(0, bankInfoItem);
        // GUI item for bank loans
        ItemStack bankLoansItem = this.createGuiItem(Material.EMERALD_BLOCK, "§2§lBank Loans",
                "cmd#" + encodeCmd("bm loans"),
                "§7Click to view bank loans");
        inventory.setItem(1, bankLoansItem);
        // GUI item for bank loans offers
        ItemStack bankLoansOffersItem = this.createGuiItem(Material.NAME_TAG, "§2§lBank Loan Offers",
                "cmd#" + encodeCmd("bm loans offers"), "§7Click to view bank loan offers");
        inventory.setItem(2, bankLoansOffersItem);

        // GUI Item for bank accounts
        ItemStack bankAccountsItem = this.createGuiItem(Material.PLAYER_HEAD, "§2§lBank Accounts",
                "cmd#" + encodeCmd("bm accounts"), "§7Click to view bank accounts");
        inventory.setItem(3, bankAccountsItem);

        // close window
        ItemStack closeItem = this.createGuiItem(Material.BARRIER, "§c§lClose", "cmd#close",
                "§7Click to close");
        inventory.setItem(8, closeItem);

    }

    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {
        String title = e.getView().getTitle();
        // get the player
        Player player = (Player) e.getWhoClicked();
        try {
            if (title.equals(GUITitles.get("bankManager"))) {
                e.setCancelled(true);
                if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                    return;

                String[] identifier = getIdentifier(e.getCurrentItem()).split("#");
                // get identifier name
                String identifierName = identifier[0];
                String identifierValue = identifier.length > 1 ? identifier[1] : "";
                String identifierBackCmd = identifier.length > 2 ? identifier[2] : "";
                NationsPlusEconomy.LOGGER.info(identifierName);
                NationsPlusEconomy.LOGGER.info(identifierValue);
                NationsPlusEconomy.LOGGER.info(identifierBackCmd);
                switch (identifierName) {
                    case "loan": {

                        player.performCommand("bm loans info " + identifierValue + " " + encodeCmd("bm loans"));
                        break;
                    }
                    case "cmd": {
                        player.closeInventory();

                        player.performCommand(decodeCmd(identifierValue) + " " + identifierBackCmd);

                        break;
                    }
                    case "accept_loan": {
                        player.closeInventory();
                        player.performCommand("bm loans offers accept " + identifierValue);
                        break;
                    }
                    case "decline_loan": {
                        player.closeInventory();

                        player.performCommand("bm loans offers decline " + identifierValue);
                        break;
                    }
                    case "cancel_loan": {
                        player.closeInventory();

                        player.performCommand("bm loans cancel " + identifierValue);
                        break;
                    }
                }

            }
        } catch (Error error) {
            handleError(player, error);
        }
    }
}
