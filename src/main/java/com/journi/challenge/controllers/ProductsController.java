package com.journi.challenge.controllers;

import com.journi.challenge.CurrencyConverter;
import com.journi.challenge.models.LocalizedProduct;
import com.journi.challenge.models.Product;
import com.journi.challenge.repositories.ProductsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductsController {

    @Inject
    private ProductsRepository productsRepository;

    @Inject
    private CurrencyConverter currencyConverter;

    @GetMapping("/products")
    public List<LocalizedProduct> list(@RequestParam(name = "countryCode", defaultValue = "AT") String countryCode) {
        String currencyCode = currencyConverter.getCurrencyForCountryCode(countryCode);
        return productsRepository
                .list().stream()
                .map(p -> convert(p, currencyCode))
                .collect(Collectors.toList());
    }

    private LocalizedProduct convert(Product p, String currencyCode) {

        // IDEA: In a very performant world, prices are precalculated already whenever conversion rates change (write once, read many) or we have a cache

        Double price = currencyCode == "EUR" ? p.getPrice() : currencyConverter.convertEurToCurrency(currencyCode, p.getPrice());
        return new LocalizedProduct(p.getId(), p.getDescription(), price, currencyCode);
    }
}
