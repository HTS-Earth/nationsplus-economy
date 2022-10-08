package com.ollethunberg.GUI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ollethunberg.utils.WalletBalanceHelper;

public class GUIManager extends WalletBalanceHelper {

    /*
     * bank: "§2§l$$ §r§a§lBank §r§2§l$$"
     * 
     */
    protected Map<String, String> GUITitles = new HashMap<String, String>();

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

    protected static int rowsToSize(int rows) {
        return rows * 9;
    }

    protected static String convertToInvisibleString(String s) {
        return ChatColor.BLACK + s;
    };

    protected String getIdentifier(ItemStack item) {
        try {
            //
            List<String> lore = item.getItemMeta().getLore();
            String identifier = lore.get(lore.size() - 1);

            identifier = ChatColor.stripColor(identifier);
            return identifier;
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

    }

    protected void handleError(Player player, Error error) {
        player.sendMessage("§r[§4§lERROR§r]§c " + error.getMessage());
    }

}
