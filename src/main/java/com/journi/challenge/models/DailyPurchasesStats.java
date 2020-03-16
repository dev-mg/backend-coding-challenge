package com.journi.challenge.models;

import java.time.LocalDateTime;
import java.util.Date;

public class DailyPurchasesStats {

    private final LocalDateTime date;
    private final Long countPurchases;
    private final Double totalAmount;
    private final Double avgAmount;
    private final Double minAmount;
    private final Double maxAmount;

    public DailyPurchasesStats(LocalDateTime date, Long countPurchases, Double totalAmount, Double avgAmount, Double minAmount, Double maxAmount) {
        this.date = date;
        this.countPurchases = countPurchases;
        this.totalAmount = totalAmount;
        this.avgAmount = avgAmount;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Long getCountPurchases() {
        return countPurchases;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Double getAvgAmount() {
        return avgAmount;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

}
