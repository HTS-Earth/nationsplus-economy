package com.ollethunberg.GUI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        System.out.println("GUIManager.convertToInvisibleString() " + s);
        String hidden = "";

        for (char c : s.toCharArray())
            hidden += ChatColor.COLOR_CHAR + "" + c;
        return hidden;
    }

    protected static String convertFromInvisibleString(String s) {
        return s.replaceAll("(?i)" + ChatColor.COLOR_CHAR, "");
    }

}
