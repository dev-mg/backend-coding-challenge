package com.journi.challenge.controllers;

import com.journi.challenge.models.DailyPurchasesStats;
import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseStats;
import com.journi.challenge.repositories.DailyPurchasesStatsRepository;
import com.journi.challenge.repositories.PurchasesRepository;
import com.journi.challenge.services.PurchasesStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PurchasesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PurchasesStatsService purchasesStatsService;
    @Autowired
    private PurchasesController purchasesController;
    @Autowired
    private PurchasesRepository purchasesRepository;
    @Autowired
    private DailyPurchasesStatsRepository dailyPurchasesStatsRepository;

    private String getPurchaseJson(String invoiceNumber, String customerName, String dateTime, Double totalValue, String currencyCode, String... productIds) {
        String productIdList = "[\"" + String.join("\",\"", productIds) + "\"]";
        return String.format(Locale.US,"{\"invoiceNumber\":\"%s\",\"customerName\":\"%s\",\"dateTime\":\"%s\",\"productIds\":%s,\"amount\":%.2f,\"currencyCode\":\"%s\"}", invoiceNumber, customerName, dateTime, productIdList, totalValue, currencyCode);
    }

    @Test
    public void testPurchaseCurrencyCodeEUR() throws Exception {
        String body = getPurchaseJson("1", "customer 1", "2020-01-01T10:00:00+01:00", 25.34, "EUR", "product1");
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk());

        Purchase savedPurchase = purchasesRepository.list().get(purchasesRepository.list().size() - 1);
        assertEquals("customer 1", savedPurchase.getCustomerName());
        assertEquals("1", savedPurchase.getInvoiceNumber());
        assertEquals("2020-01-01T10:00:00", savedPurchase.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        assertEquals(25.34, savedPurchase.getTotalValue());
    }

    @Test
    public void testPurchaseCurrencyCodeUSD() throws Exception {
        String body = getPurchaseJson("2", "customer 2", "2020-01-02T10:00:00+01:00", 25.0 * 5.1480, "BRL", "product1");
        mockMvc.perform(post("/purchases")
                .contentType(MediaType.APPLICATION_JSON).content(body)
        ).andExpect(status().isOk());

        Purchase savedPurchase = purchasesRepository.list().get(purchasesRepository.list().size() - 1);
        assertEquals("customer 2", savedPurchase.getCustomerName());
        assertEquals("2", savedPurchase.getInvoiceNumber());
        assertEquals("2020-01-02T10:00:00", savedPurchase.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        assertEquals(25.0, savedPurchase.getTotalValue());
    }

    @Test
    public void testEmptyPurchaseStatistics() {
        // Test case when no purchases saved at all
    }

    @Test
    public void testPurchaseStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDate = now.minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));
        // Inside window purchases
        purchasesRepository.save(new Purchase("1", firstDate, Collections.emptyList(), "", 1.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(1), Collections.emptyList(), "", 2.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(2), Collections.emptyList(), "", 3.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(3), Collections.emptyList(), "", 4.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(4), Collections.emptyList(), "", 5.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(5), Collections.emptyList(), "", 6.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(6), Collections.emptyList(), "", 7.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(7), Collections.emptyList(), "", 8.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(8), Collections.emptyList(), "", 9.0));
        purchasesRepository.save(new Purchase("1", firstDate.plusDays(9), Collections.emptyList(), "", 10.0));

        // Outside window purchases

        // TODO: is it really outside? or is day-30 the outsider?
        purchasesRepository.save(new Purchase("1", now, Collections.emptyList(), "", 100000.0));

        purchasesRepository.save(new Purchase("1", firstDate.minusDays(1), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.minusDays(2), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.minusDays(3), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.minusDays(4), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.minusDays(5), Collections.emptyList(), "", 10.0));
        purchasesRepository.save(new Purchase("1", firstDate.minusDays(6), Collections.emptyList(), "", 10.0));

        // In reality: daily maintenance task or CQRS-architecture pattern
        // FIXME those hardcoded days are fishy
        for (LocalDateTime dateTime = firstDate.minusDays(7); dateTime.isBefore(firstDate.plusDays(10)); dateTime = dateTime.plusDays(1))
        {
            DailyPurchasesStats stat = purchasesStatsService.getDailyStats(dateTime);
            if(stat != null) {
                dailyPurchasesStatsRepository.save(stat);
            }
        }

        PurchaseStats purchaseStats = purchasesController.getStats();
        assertEquals(formatter.format(firstDate), purchaseStats.getFrom());
        assertEquals(formatter.format(firstDate.plusDays(9)), purchaseStats.getTo());
        assertEquals(10, purchaseStats.getCountPurchases());
        assertEquals(55, purchaseStats.getTotalAmount());
        assertEquals(5.5, purchaseStats.getAvgAmount());
        assertEquals(1.0, purchaseStats.getMinAmount());
        assertEquals(10.0, purchaseStats.getMaxAmount());
    }
}
