package com.example.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageResponse {

    @JsonProperty("Time Series (Daily)")
    private Map<String, DataOnTheValueOfTheShares> dataOnTheValueOfTheShares;

    public Map<String, DataOnTheValueOfTheShares> getDataOnTheValueOfTheShares() {
        return dataOnTheValueOfTheShares;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataOnTheValueOfTheShares {
        @JsonProperty("4. close")
        private String close;

        public String getClose() {
            return close;
        }

        @JsonProperty("8. split coefficient")
        private String splitCoefficient;

        public String getSplitCoefficient() {
            return splitCoefficient;
        }
    }
}
