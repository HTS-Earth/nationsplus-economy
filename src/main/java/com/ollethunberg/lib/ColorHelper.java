package com.ollethunberg.lib;

public class ColorHelper {
    public static String getColorOfPercentage(float value) {
        if (value >= 0.8) {
            return "§a";
        } else if (value >= 0.6) {
            return "§e";
        } else if (value >= 0.4) {
            return "§6";
        } else if (value >= 0.2) {
            return "§c";
        } else {
            return "§4";
        }
    }
}
