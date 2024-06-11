import java.util.ArrayList;
import java.util.List;

public class movingAverage {
    public static List<Double> calculateMovingAverage(double[] stock_price, int startingDate, int endingDate) {
        List<Double> movingAverage = new ArrayList<>();
        double sum = 0;
        int timeFrame = endingDate - startingDate + 1;
        int currentDate;
        double result;

        for (int i = 0; i < timeFrame - 4; i++) {
            currentDate = startingDate;
            for (int j = 0;j < 5; j++) {
                sum += stock_price[currentDate];
                currentDate++;
            }
            startingDate++;
            result = sum/5;
            movingAverage.add(result);
            sum = 0;
        }

        return movingAverage;
    }
}
