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
import com.ollethunberg.commands.loan.LoanHelper;
import com.ollethunberg.commands.loan.models.DBLoan;
import com.ollethunberg.database.DBPlayer;
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
            String paidToTotalRateColor = ColorHelper.getColorFromPercentage(loan.amount_paid / loan.amount_total);
            /* Create GUI item */
            ItemStack item = this.createGuiItem(loan.active ? Material.EMERALD_BLOCK : Material.NAME_TAG,
                    (loan.active ? "§2§lACTIVE Loan #" : "§aLoan #") + loan.id, "loan_" + loan.id,
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

            /* Add item to inventory */
            inventory.setItem(i, item);
        }
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
            }
        } catch (Error error) {
            handleError(player, error);
        }
    }
}
