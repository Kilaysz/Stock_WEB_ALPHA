package com.Website.StockWeb;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.Website.StockWeb.function.CalculateResistanceAndSupport;
import com.Website.StockWeb.function.LineRegression;
import com.Website.StockWeb.function.MovingAverage;
import com.Website.StockWeb.function.StandardDeviation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class StockDataController {
    private static final Logger logger = Logger.getLogger(StockDataController.class.getName());

    private final AlphaVantageService alphaVantageService;

    public StockDataController(AlphaVantageService alphaVantageService) {
        this.alphaVantageService = alphaVantageService;
    }

    // Helper method to validate payload
    private void validatePayload(Map<String, String> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be null or empty");
        }
        if (payload.get("company") == null || payload.get("company").isEmpty()) {
            throw new IllegalArgumentException("Company symbol is required");
        }
        if (payload.get("startDate") == null || payload.get("startDate").isEmpty()) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (payload.get("endDate") == null || payload.get("endDate").isEmpty()) {
            throw new IllegalArgumentException("End date is required");
        }
    }

    // Helper method to get closing prices
    private Map<String, Double> getClosingPrices(String company, String startDate, String endDate) throws IOException {
        long startEpoch = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
                .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long endEpoch = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
                .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        return alphaVantageService.getClosingPrices(company, startEpoch, endEpoch);
    }

    // Endpoint to fetch stock data and return closing prices
    @PostMapping(value = "/stockData", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStockData(@RequestBody Map<String, String> payload) {
        try {
            validatePayload(payload);
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            Map<String, Double> closingPrices = getClosingPrices(company, startDate, endDate);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("closingPrices", closingPrices);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid input: " + e.getMessage());
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error fetching stock data", e);
            return new ResponseEntity<>(Map.of("error", "Failed to fetch stock data"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate the standard deviation of closing prices
    @PostMapping(value = "/calculateStandardDeviation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> calculateStandardDeviation(@RequestBody Map<String, String> payload) {
        try {
            validatePayload(payload);
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            Map<String, Double> closingPrices = getClosingPrices(company, startDate, endDate);
            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            double standardDeviation = StandardDeviation.calculateStandardDeviation(pricesArray);

            return new ResponseEntity<>(Map.of("standardDeviation", standardDeviation), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid input: " + e.getMessage());
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error calculating standard deviation", e);
            return new ResponseEntity<>(Map.of("error", "Failed to calculate standard deviation"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate the linear regression of closing prices
    @PostMapping(value = "/calculateLineRegression", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> calculateLineRegression(@RequestBody Map<String, String> payload) {
        try {
            validatePayload(payload);
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            Map<String, Double> closingPrices = getClosingPrices(company, startDate, endDate);
            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            Map<String, Object> regressionResult = LineRegression.calculateLineRegression(pricesArray);

            return new ResponseEntity<>(regressionResult, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid input: " + e.getMessage());
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error calculating line regression", e);
            return new ResponseEntity<>(Map.of("error", "Failed to calculate line regression"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate resistance and support levels of closing prices
    @PostMapping(value = "/calculateResistanceAndSupport", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> calculateResistanceAndSupport(@RequestBody Map<String, String> payload) {
        try {
            validatePayload(payload);
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");

            Map<String, Double> closingPrices = getClosingPrices(company, startDate, endDate);
            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            Map<String, Double> result = CalculateResistanceAndSupport.calculateResistanceAndSupport(pricesArray);

            return new ResponseEntity<>(Map.of("resistanceAndSupport", result), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid input: " + e.getMessage());
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error calculating resistance and support", e);
            return new ResponseEntity<>(Map.of("error", "Failed to calculate resistance and support"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to calculate the moving average of closing prices
    @PostMapping(value = "/calculateMovingAverage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> calculateMovingAverage(@RequestBody Map<String, String> payload) {
        try {
            validatePayload(payload);
            String company = payload.get("company");
            String startDate = payload.get("startDate");
            String endDate = payload.get("endDate");
            int period = Integer.parseInt(payload.get("period"));

            Map<String, Double> closingPrices = getClosingPrices(company, startDate, endDate);
            double[] pricesArray = closingPrices.values().stream().mapToDouble(Double::doubleValue).toArray();
            double[] movingAverage = MovingAverage.calculateMovingAverage(pricesArray, period);

            return new ResponseEntity<>(Map.of("movingAverage", movingAverage), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid input: " + e.getMessage());
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error calculating moving average", e);
            return new ResponseEntity<>(Map.of("error", "Failed to calculate moving average"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
// SZ1T0VSO6EFNL8JR

@Service
class AlphaVantageService {
    private static final Logger logger = Logger.getLogger(StockDataController.class.getName());
    public static final String API_KEY = "OGP3DMV2N9J7G85W";
    public static final String BASE_URL = "https://www.alphavantage.co/query";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AlphaVantageService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Double> getClosingPrices(String symbol, long startEpoch, long endEpoch) throws IOException {
        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s", BASE_URL, symbol, API_KEY);
        String jsonData = restTemplate.getForObject(url, String.class);

        // Log the API response for debugging
        logger.info("Alpha Vantage API Response: " + jsonData);

        return extractClosingPrices(jsonData);
    }

    private Map<String, Double> extractClosingPrices(String jsonData) throws IOException {
        Map<String, Double> closingPrices = new LinkedHashMap<>();
        Map<String, Object> response = objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {});

        // Check for error messages in the response
        if (response.containsKey("Error Message")) {
            String errorMessage = "Alpha Vantage API Error: " + response.get("Error Message");
            logger.severe(errorMessage);
            throw new IOException(errorMessage);
        }
        if (response.containsKey("Note")) {
            String rateLimitMessage = "Alpha Vantage API Rate Limit Exceeded: " + response.get("Note");
            logger.severe(rateLimitMessage);
            throw new IOException(rateLimitMessage);
        }

        // Check if the response contains the expected data
        if (!response.containsKey("Time Series (Daily)")) {
            String noDataMessage = "No time series data found in the API response. Response: " + response;
            logger.severe(noDataMessage);
            throw new IOException(noDataMessage);
        }

        Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response.get("Time Series (Daily)");
        for (Map.Entry<String, Map<String, String>> entry : timeSeries.entrySet()) {
            String date = entry.getKey();
            double closingPrice = Double.parseDouble(entry.getValue().get("4. close"));
            closingPrices.put(date, closingPrice);
        }
        return closingPrices;
    }
}