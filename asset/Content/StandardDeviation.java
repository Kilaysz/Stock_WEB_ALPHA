import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.stream;
import java.lang.math;
import java.util.Arrays;
import java.io.IOException;

public class StandardDeviation{
    public static double calculateStandardDeviation(double[] prices, String stock, int start, int end) {
        int n = prices.length;//retrieves the number of elements in the prices array

        //Convert the prices array into a stream and Calculate the average
        //If the array is empty it returns 0.0 as a default value, else it returns the calculated average
        double mean = Arrays.stream(prices).average().orElse(0.0);

        //convert the prices array into a stream
        //calculate the squared deviation of each price from the mean
        double sumSquaredDeviations = Arrays.stream(prices).map(val -> Math.pow(val - mean, 2)).sum();

        double s = Math.sqrt(sumSquaredDeviations / (n - 1));
        
        return s;
    }          
}