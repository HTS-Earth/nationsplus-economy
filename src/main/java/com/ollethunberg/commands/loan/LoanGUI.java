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
import com.ollethunberg.lib.ColorHelper;

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
        inventory = Bukkit.createInventory(null, rowsToSize(4), GUITitles.get("loans"));
        this.initializeLoansGUIItems(loans);
        player.openInventory(inventory);

    }

    private void initializeLoansGUIItems(List<DBLoan> loans) {
        /* Create GUI items for each loan, using for loop with index */
        for (int i = 0; i < loans.size(); i++) {
            DBLoan loan = loans.get(i);

            String paidToTotalRateColor = ColorHelper.getColorFromPercentage(loan.amount_paid / loan.amount_total);
            /* Create GUI item */
            ItemStack item = this.createGuiItem(loan.active ? Material.EMERALD_BLOCK : Material.NAME_TAG,
                    (loan.active ? "§2§lACTIVE Loan #" : "§aLoan #") + loan.id, "loan_name",
                    "§7Interest rate: §r§l§a" + loan.interest_rate * 100 + "%",
                    "§7Amount paid: §r" + paidToTotalRateColor
                            + NationsPlusEconomy.dollarFormat.format(loan.amount_paid) + "§f/§a"
                            + NationsPlusEconomy.dollarFormat.format(loan.amount_total),
                    "§7Paid off percentage: " + paidToTotalRateColor
                            + (int) (loan.amount_paid / loan.amount_total * 100) + "%",
                    "§7Total payments: §r§l§a" + loan.payments_total,
                    "§7Left payments: §r§l§a" + loan.payments_left,
                    loan.active ? "§7Cost per hour: §r§l§a"
                            + NationsPlusEconomy.dollarFormat.format(loanHelper.getCostPerHour(loan)) : "");

            /* Add item to inventory */
            inventory.setItem(i, item);
        }
        // Item for creating a new loan
        ItemStack item = this.createGuiItem(Material.GOLD_BLOCK, "§6§lApply for a new loan", "new_loan",
                "§f> §7Click to get help on how to apply for a new loan");
        inventory.setItem(35, item);
    }

    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {
        String title = e.getView().getTitle();
        // get the player
        Player player = (Player) e.getWhoClicked();
        try {
            if (title.equals(GUITitles.get("loans"))) {
                e.setCancelled(true);
                if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                    return;

                // Get the clicked item identifer
                String itemIdentifier = this.getIdentifier(e.getCurrentItem());
                if (itemIdentifier.equals("new_loan")) {
                    player.closeInventory();

                    player.sendMessage(
                            "§c§lHere are some key points to know about loans:§r",
                            "§6 - Loans are paid off in hourly payments",
                            "§6 - A loan has an interest-rate. The interest rate is the amount you are charged for borrowing money",
                            "§6 - The interest amount is calculated by multiplying the interest rate with the amount you still owe",
                            "§6 - For example, if you owe $1000 and you have an interest rate of 10%, you will be charged an additional $100 every hour",
                            "§6 - The loan's payments quantity is the amount of hours to pay off the loan",
                            "§6 - The total amount you have to pay per hour is: (<loan amount> / <payments quantity> ) + (<interest rate> * <loan amount left>)",
                            "§6 - The money you pay is taken from your bank account or your wallet balance",
                            "§6 - A bank has to accept your loan application before you can get the loan",
                            "§7> §fType §a/loan apply <amount> <interest rate> <payments quantity>§f to apply for a new loan at your current bank");
                }

            }
        } catch (Error error) {
            handleError(player, error);
        }
    }
}
