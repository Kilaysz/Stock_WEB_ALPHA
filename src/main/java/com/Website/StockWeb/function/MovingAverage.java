package com.Website.StockWeb.function;


public class MovingAverage {
    public static double[] calculateMovingAverage(double[] prices, int period) {
        double[] movingAverages = new double[prices.length - period + 1];//Set the moving average acquisition period
        for (int i = 0; i < movingAverages.length; i++) {
            double sum = 0.0;
            for (int j = i; j < i + period; j++) {
                sum += prices[j];
            }
            movingAverages[i] = sum / period;
        }
        return movingAverages;
    }
}
