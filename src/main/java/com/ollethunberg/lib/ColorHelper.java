package com.ollethunberg.lib;

import java.text.DecimalFormat;

public class ColorHelper {
    public static String addColorToPercentage(float value) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

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

        return color + df.format(value * 100) + "%";

    }
}
