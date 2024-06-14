package com.Website.StockWeb.function;
import java.util.*;
import java.util.stream.Collectors;

public class CalculateResistanceAndSupport {

    public static Map<String, Double> calculateResistanceAndSupport(double[] closingPrices) {
        if (closingPrices == null || closingPrices.length == 0) {
            throw new IllegalArgumentException("Closing prices must not be null or empty");
        }

        double maxPrice = Double.NEGATIVE_INFINITY;
        double minPrice = Double.POSITIVE_INFINITY;
        for (double price : closingPrices) {
            if (price > maxPrice) maxPrice = price;
            if (price < minPrice) minPrice = price;
        }

        List<Double> sortedPrices = Arrays.stream(closingPrices).boxed().sorted().collect(Collectors.toList());
        int n = sortedPrices.size();
        double q1 = sortedPrices.get(n / 4);
        double q3 = sortedPrices.get(3 * n / 4);

        Map<String, Double> result = new TreeMap<>();
        result.put("MajorResistance", maxPrice);
        result.put("MajorSupport", minPrice);
        result.put("MinorResistance", q3);
        result.put("MinorSupport", q1);

        return result;
    }
}
