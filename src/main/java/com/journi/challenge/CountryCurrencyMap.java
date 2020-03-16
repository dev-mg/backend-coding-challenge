package com.journi.challenge;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;

@Named
@Singleton
public class CountryCurrencyMap extends HashMap<String, String> {
    public CountryCurrencyMap() {
        put("AT", "EUR");
        put("DE", "EUR");
        put("HU", "HUF");
        put("GB", "GBP");
        put("FR", "EUR");
        put("PT", "EUR");
        put("IE", "EUR");
        put("ES", "EUR");
        put("BR", "BRL");
        put("US", "USD");
        put("CA", "CAD");
    }
}
