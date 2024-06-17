package com.Website.StockWeb.function;

import java.lang.Math;
import java.util.Arrays;

public class StandardDeviation{
    public static double calculateStandardDeviation(double[] prices) {
        int n = prices.length;

    
        double mean = Arrays.stream(prices).average().orElse(0.0);

    
        double sumSquaredDeviations = Arrays.stream(prices).map(val -> Math.pow(val - mean, 2)).sum(); //acquisition average, if null return 0
        // Math.pow(val - mean, 2) ← extracting each element of value from array then subtracting mean(average) after that squared then summing
        double s = Math.sqrt(sumSquaredDeviations / (n - 1));
        
        return s;
    }          
}