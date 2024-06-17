package com.Website.StockWeb;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.Website.StockWeb.function.CalculateResistanceAndSupport;
import com.Website.StockWeb.function.LineRegression;
import com.Website.StockWeb.function.MovingAverage;
import com.Website.StockWeb.function.StandardDeviation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class StockDataController {

    private static final Logger logger = Logger.getLogger(StockDataController.class.getName());

    // Endpoint to fetch stock data and return closing prices
    @PostMapping(value = "/stockData", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStockData(@RequestBody Map<String, String> payload) {
        try {
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            // Date to epoch timestamp conversion
            long startEpoch = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long endEpoch = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            
            // Fetch stock data
            String stockData = fetchStockData(company, startEpoch, endEpoch);
            Map<String, Double> closingPrices = extractClosingPrices(stockData);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("closingPrices", closingPrices);

            // Return the response to the frontend
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Error handling
            logger.log(Level.SEVERE, "Error processing request", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate the standard deviation of closing prices
    @PostMapping(value = "/calculateStandardDeviation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Double> calculateStandardDeviation(@RequestBody Map<String, String> payload) {
        try {
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            // Date to epoch timestamp conversion
            long startEpoch = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long endEpoch = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();

            // Fetch stock data
            String stockData = fetchStockData(company, startEpoch, endEpoch);
            Map<String, Double> closingPrices = extractClosingPrices(stockData);

            // Convert closingPrices map values to array
            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            double standardDeviation = StandardDeviation.calculateStandardDeviation(pricesArray);
            return new ResponseEntity<>(standardDeviation, HttpStatus.OK);
        } catch (Exception e) {
            // Error handling
            logger.log(Level.SEVERE, "Error calculating standard deviation", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate the linear regression of closing prices
    @PostMapping(value = "/calculateLineRegression", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> calculateLineRegression(@RequestBody Map<String, String> payload) {
        try {
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            // Date to epoch timestamp conversion
            long startEpoch = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long endEpoch = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();

            // Fetch stock data
            String stockData = fetchStockData(company, startEpoch, endEpoch);
            Map<String, Double> closingPrices = extractClosingPrices(stockData);

            // Convert closingPrices map values to array
            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            Map<String, Object> regressionResult = LineRegression.calculateLineRegression(pricesArray);

            return new ResponseEntity<>(regressionResult, HttpStatus.OK);
        } catch (Exception e) {
            // Error handling
            logger.log(Level.SEVERE, "Error calculating line regression", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate resistance and support levels of closing prices
    @PostMapping(value = "/calculateResistanceAndSupport", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Double>> calculateResistanceAndSupport(@RequestBody Map<String, String> payload) {
        try {
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            // Date to epoch timestamp conversion
            long startEpoch = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long endEpoch = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();

            // Fetch stock data
            String stockData = fetchStockData(company, startEpoch, endEpoch);
            Map<String, Double> closingPrices = extractClosingPrices(stockData);

            // Convert closingPrices map values to array
            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            Map<String, Double> result = CalculateResistanceAndSupport.calculateResistanceAndSupport(pricesArray);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            // Error handling
            logger.log(Level.SEVERE, "Error calculating resistance and support", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate the moving average of closing prices
    @PostMapping(value = "/calculateMovingAverage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<double[]> calculateMovingAverage(@RequestBody Map<String, String> payload) {
        try {
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");
            int period = Integer.parseInt(payload.get("period"));

            // Date to epoch timestamp conversion
            long startEpoch = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long endEpoch = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();

            // Fetch stock data
            String stockData = fetchStockData(company, startEpoch, endEpoch);
            Map<String, Double> closingPrices = extractClosingPrices(stockData);

            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            double[] movingAverage = MovingAverage.calculateMovingAverage(pricesArray, period);

            return new ResponseEntity<>(movingAverage, HttpStatus.OK);
        } catch (Exception e) {
            // Error handling    
            logger.log(Level.SEVERE, "Error calculating moving average", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Function to fetch stock data from Yahoo Finance API
    private String fetchStockData(String company, long startDate, long endDate) {
        String urlStr = String.format("https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=history",
                company, startDate, endDate);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(urlStr, String.class);
    }

    // Function to extract closing prices from CSV data
    private Map<String, Double> extractClosingPrices(String csvData) throws IOException {
        Map<String, Double> closingPrices = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new StringReader(csvData));
        String line;

        // Skip header line
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            String date = fields[0];
            // get the closing price from the all prices caught by API
            double closingPrice = Double.parseDouble(fields[4]);
            closingPrices.put(date, closingPrice);
        }

        return closingPrices;
    }
}
