package com.Website.StockWeb.function;

import java.lang.Math;
import java.util.Arrays;

public class StandardDeviation{
    public static double calculateStandardDeviation(double[] prices) {
        int n = prices.length;

    
        double mean = Arrays.stream(prices).average().orElse(0.0);

    
        double sumSquaredDeviations = Arrays.stream(prices).map(val -> Math.pow(val - mean, 2)).sum();

        double s = Math.sqrt(sumSquaredDeviations / (n - 1));
        
        return s;
    }          
}