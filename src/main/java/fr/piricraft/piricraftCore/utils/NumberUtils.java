package fr.piricraft.piricraftCore.utils;

import java.text.DecimalFormat;

public class NumberUtils {

    private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("#,##0.00");

    public static String formatMoney(double amount) {
        return (CURRENCY_FORMATTER.format(amount) + " €");
    }

}
