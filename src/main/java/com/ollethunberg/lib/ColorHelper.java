package com.ollethunberg.lib;

public class ColorHelper {
    public static String addColorToPercentage(float value) {
        String color = "";
        if (value >= 0.8) {
            color = "§a";
        } else if (value >= 0.6) {
            color = "§e";
        } else if (value >= 0.4) {
            color = "§6";
        } else if (value >= 0.2) {
            color = "§c";
        } else {
            color = "§4";
        }
        return color + value + "%";
    }
}
