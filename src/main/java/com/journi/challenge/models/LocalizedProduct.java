package com.journi.challenge.models;

public class LocalizedProduct extends Product {

    private final String currencyCode;

    public LocalizedProduct(String id, String description, Double price, String currencyCode) {
        super(id, description, price);
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
