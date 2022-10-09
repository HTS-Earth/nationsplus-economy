package com.ollethunberg.lib;

import java.text.DecimalFormat;

public class ColorHelper {
    public static String getColorFromPercentage(float value) {
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

    public static String addColorToPercentage(float value) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        String color = getColorFromPercentage(value);

        return color + df.format(value * 100) + "%";

    }
}
