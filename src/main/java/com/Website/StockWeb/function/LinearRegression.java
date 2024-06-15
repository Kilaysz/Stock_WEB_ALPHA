package com.Website.StockWeb.function;

import java.util.HashMap;
import java.util.Map;

public class LinearRegression {
    public static Map<String, Object> calculateLineRegression(double[] stockPrices) {
        double startDouble = 1.00d;
        double endDouble = stockPrices.length;
        double tBar = (endDouble + startDouble) / 2;
        double sum = 0.00d;
        for (Double j : stockPrices) {
            sum += j;
        }
        System.out.println(sum);
        double yBar = sum / (double)(stockPrices.length);
        System.out.println(yBar);
        // Calculate b1
        double numerator = 0.00d;
        double denominator = 0.00d;
        double t = startDouble;
        for (Double j : stockPrices) {
            numerator += (t - tBar) * (j - yBar);
            denominator += (t - tBar) * (t - tBar);
            t = t + 1.0000d;
        }

        double b1 = numerator / denominator;
        // calculate b0
        double b0 = yBar - (b1 * tBar);
        
        Map<String, Object> result = new HashMap<>();
        result.put("b0", b0);
        result.put("b1", b1);
        result.put("stockPrices", stockPrices);
        return result;
    }
}