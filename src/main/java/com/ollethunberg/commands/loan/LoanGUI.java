package com.ollethunberg.commands.loan;

import java.sql.SQLException;
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
import com.ollethunberg.commands.loan.models.DBLoan;

public class LoanGUI extends GUIManager implements Listener {
    private Inventory inventory;
    LoanHelper loanHelper;

    public LoanGUI() {

        /* Register */
        GUITitles.put("loans", "§2Loans");

        /* Initialize helpers */
        loanHelper = new LoanHelper();
    }

    public void loans(Player player) throws SQLException, Error {

        List<DBLoan> loans = loanHelper.getLoansFromPlayer(player);
        inventory = Bukkit.createInventory(null, rowsToSize(3), GUITitles.get("loans"));
        this.initializeLoansGUIItems(loans);
        player.openInventory(inventory);

    }

    private void initializeLoansGUIItems(List<DBLoan> loans) {
        /* Create GUI items for each loan, using for loop with index */
        for (int i = 0; i < loans.size(); i++) {
            DBLoan loan = loans.get(i);
            /* Create GUI item */
            ItemStack item = this.createGuiItem(loan.active ? Material.EMERALD_BLOCK : Material.NAME_TAG,
                    (loan.active ? "§2§lACTIVE Loan #" : "§aLoan #") + loan.id, "loan_name",
                    "§7Loan interest: §r§l§a" + loan.interest_rate * 100 + "%",
                    "§7Loan amount: §r§l§a" + NationsPlusEconomy.dollarFormat.format(loan.amount_total) + "§r§l§a",
                    "§7Loan paid: §r§l§a" + NationsPlusEconomy.dollarFormat.format(loan.amount_paid) + "§r§l§a",
                    "§7Amount paid off: §r§l§a" + (int) (loan.amount_paid / loan.amount_total * 100) + "%",
                    "§7Loan duration: §r§l§a" + loan.payments_total + "§r§l§a",
                    "§7Loan remaining: §r§l§a" + loan.payments_left + "§r§l§a");
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
            if (title.equals(GUITitles.get("loans"))) {
                e.setCancelled(true);
            }
        } catch (Error error) {
            handleError(player, error);
        }
    }
}
