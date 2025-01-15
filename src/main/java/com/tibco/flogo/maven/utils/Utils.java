package com.tibco.flogo.maven.utils;

import java.text.DecimalFormat;

public class Utils {

    private static DecimalFormat format = new DecimalFormat("#.##");

    public static String getPercentage(int total, int failure) {
        float successRate = 0;
        if ( total == 0 && failure == 0 ) {
            successRate = 0;
        }  else if (failure == 0) {
            successRate = 100;
        } else if (total == failure) {
            successRate = 0;
        } else {
            successRate = ((float) (((total - failure)) / (float) total)) * 100;
        }

        return String.valueOf(format.format(successRate)) + "%";
    }
}
