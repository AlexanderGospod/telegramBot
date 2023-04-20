package com.example.client;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.example.pojo.AlphaVantageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class AlphaVantageApiClient {
    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageApiClient.class);
    private final String API_KEY;
    private final String BASE_URI;
    private final String ENDPOINT;
    public AlphaVantageApiClient()  {
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            props.load(input);
            this.API_KEY = props.getProperty("alphaVantageApiKey");
            this.BASE_URI = props.getProperty("baseUri");
            this.ENDPOINT = props.getProperty("endpoint");
        } catch (NullPointerException | IOException e) {
            logger.error("Unable to read properties from the configuration file in the resources section", e);
            throw new NullPointerException();
        }
    }
    public AlphaVantageResponse getStockData(String company) {
        Response response = RestAssured.given()
                .param("function", "TIME_SERIES_DAILY_ADJUSTED")
                .param("symbol", company)
                .param("apikey", API_KEY)
                .param("outputsize", "full")
                .param("time_period", "60")
                .when()
                .get( BASE_URI + ENDPOINT)
                .then()
                .extract()
                .response();
        return response.as(AlphaVantageResponse.class);
    }
    public String getTickerSymbol(String companyName){
        Response response = RestAssured.given()
                .param("function", "SYMBOL_SEARCH")
                .param("keywords", companyName)
                .param("apikey", API_KEY)
                .get(BASE_URI + ENDPOINT);

        return response.jsonPath().getString("bestMatches.find { it.'4. region' == 'United States' }.'1. symbol'");
    }
}