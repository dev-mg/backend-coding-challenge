package com.journi.challenge.controllers;

import com.journi.challenge.CurrencyConverter;
import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseRequest;
import com.journi.challenge.models.PurchaseStats;
import com.journi.challenge.repositories.PurchasesRepository;
import com.journi.challenge.services.PurchasesStatsService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class PurchasesController {

    @Inject
    private PurchasesRepository purchasesRepository;

    @Inject
    private PurchasesStatsService purchasesStatsService;

    @Inject
    private CurrencyConverter currencyConverter;

    @GetMapping("/purchases/statistics")
    public PurchaseStats getStats() {
        return purchasesStatsService.getLast30DaysStats();
    }

    @PostMapping("/purchases")
    public Purchase save(@RequestBody PurchaseRequest purchaseRequest) {

        String currencyCode = purchaseRequest.getCurrencyCode();

        // TODO: maybe we should check the code here and return "401" if it is not valid?
        // FIXME: http 401 is "Unauthorized"... "Bad request" is 400
        // PS: this is the reason why coding challenges suck ;-)

        Double totalValue = currencyCode == "EUR" ?
                purchaseRequest.getAmount() :
                currencyConverter.convertCurrencyToEur(currencyCode, purchaseRequest.getAmount());

        Purchase newPurchase = new Purchase(
                purchaseRequest.getInvoiceNumber(),
                LocalDateTime.parse(purchaseRequest.getDateTime(), DateTimeFormatter.ISO_DATE_TIME),
                purchaseRequest.getProductIds(),
                purchaseRequest.getCustomerName(),
                totalValue
        );
        purchasesRepository.save(newPurchase);
        return newPurchase;
    }
}
