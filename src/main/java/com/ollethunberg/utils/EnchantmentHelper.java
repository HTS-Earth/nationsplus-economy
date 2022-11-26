package com.ollethunberg.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class EnchantmentHelper {

    public static String getEnchantmentsAsString(ItemStack item) {
        Map<Enchantment, Integer> enchants = item.getEnchantments();
        List<String> enchantList = new ArrayList<String>();
        for (Enchantment e : item.getEnchantments().keySet()) {
            int level = enchants.get(e);
            NamespacedKey key = e.getKey();
            String enchant = key.getKey() + ":" + level;
            enchantList.add(enchant + ":" + level);
        }
        return String.join(",", enchantList);
    }

    public static ItemStack addEnchants(ItemStack item, String enchantments) {
        List<String> enchantmentStringList = Arrays.asList(enchantments.split(","));
        for (String enchantment : enchantmentStringList) {
            if (enchantment.equals(""))
                continue;
            String[] enchantmentSplit = enchantment.split(":");
            item.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantmentSplit[0])),
                    Integer.parseInt(enchantmentSplit[1]));
        }
        return item;
    }
}
