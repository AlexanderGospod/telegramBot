package com.example.client;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BingApiClient {
    private static final Logger logger = LoggerFactory.getLogger(BingApiClient.class);
    private final String BING_API_KEY;
    private final String BING_ENDPOINT;


    public BingApiClient() {
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            props.load(input);
            BING_API_KEY = props.getProperty("bingApiKey");
            BING_ENDPOINT = props.getProperty("bingEndpoint");
        } catch (NullPointerException | IOException e) {
            logger.error("Unable to read properties from the configuration file in the resources section", e);
            throw new NullPointerException();
        }
    }

    public String getCorrectCompanyNameInEnglish(String companyName) {
        String query = companyName + "&mkt=en-US";
        Response response = RestAssured.given()
                .header("Ocp-Apim-Subscription-Key", BING_API_KEY)
                .get(BING_ENDPOINT + query);

        //Get company url
        String companyNameInEnglish = response.jsonPath().getString("webPages.value[0].url");

        // Pattern that will remove everything from the url except the company name
        Pattern pattern = Pattern.compile("^https?://(?:www\\.)?(?:en\\.)?(.+?)\\.(?:com|ru|.+)$");
        Matcher matcher = pattern.matcher(companyNameInEnglish);
        if (matcher.find())
            companyNameInEnglish = matcher.group(1);

        return companyNameInEnglish;
    }
}
