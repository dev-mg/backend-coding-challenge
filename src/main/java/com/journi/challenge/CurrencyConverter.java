package com.journi.challenge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URL;
import java.util.*;

@Named
@Singleton
public class CurrencyConverter {
    private final Map<String, String> supportedCountriesCurrency;
    private final Map<String, Double> currencyEurRate;

    public CurrencyConverter() {
        supportedCountriesCurrency = new CountryCurrencyMap();

        currencyEurRate = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            URL rates = getClass().getResource("/eur_rate.json");
            JsonNode ratesTree = mapper.readTree(rates);
            Iterator<JsonNode> currenciesIterator = ratesTree.findPath("currencies").elements();
            currenciesIterator.forEachRemaining(currency -> currencyEurRate.put(currency.get("currency").asText(), currency.get("rate").asDouble()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String getCurrencyForCountryCode(String countryCode) {
        return supportedCountriesCurrency.getOrDefault(countryCode.toUpperCase(), "EUR");
    }

    public Double convertEurToCurrency(String currencyCode, Double eurValue) {
        return eurValue * currencyEurRate.getOrDefault(currencyCode, 1.0);
    }

    public Double convertCurrencyToEur(String currencyCode, Double currencyValue) {
        return currencyValue / currencyEurRate.getOrDefault(currencyCode, 1.0);
    }
}
