package com.journi.challenge.services;

import com.journi.challenge.models.DailyPurchasesStats;
import com.journi.challenge.models.Purchase;
import com.journi.challenge.models.PurchaseStats;
import com.journi.challenge.repositories.DailyPurchasesStatsRepository;
import com.journi.challenge.repositories.PurchasesRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named
@Singleton
public class PurchasesStatsService {
    @Inject
    private PurchasesRepository purchasesRepository;

    @Inject
    private DailyPurchasesStatsRepository dailyPurchasesStatsRepository;

    public PurchaseStats getLast30DaysStats() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));

        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime start = today.minusDays(30);

        List<DailyPurchasesStats> recentPurchases = dailyPurchasesStatsRepository
                .list()
                .stream()
                .filter(p -> (p.getDate().equals(start) || p.getDate().isAfter(start))
                        && p.getDate().isBefore(today)) // this may be the bug you mentioned or not
                .sorted(Comparator.comparing(DailyPurchasesStats::getDate))
                .collect(Collectors.toList());

        long countPurchases = recentPurchases.stream().mapToLong(DailyPurchasesStats::getCountPurchases).sum();
        double totalAmountPurchases = recentPurchases.stream().mapToDouble(DailyPurchasesStats::getTotalAmount).sum();

        // FIXME: throws an error if no purchases are made at all

        return new PurchaseStats(
                formatter.format(recentPurchases.get(0).getDate()),
                formatter.format(recentPurchases.get(recentPurchases.size() - 1).getDate()),
                countPurchases,
                totalAmountPurchases,
                totalAmountPurchases / countPurchases, // FIXME
                recentPurchases.stream().mapToDouble(DailyPurchasesStats::getMinAmount).min().orElse(0.0),
                recentPurchases.stream().mapToDouble(DailyPurchasesStats::getMaxAmount).max().orElse(0.0)
        );
    }

    public DailyPurchasesStats getDailyStats(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"));

        List<Purchase> recentPurchases = purchasesRepository
                .list()
                .stream()
                .filter(p -> p.getTimestamp().isAfter(date.toLocalDate().atStartOfDay().minusNanos(1)) &&
                        p.getTimestamp().isBefore(date.toLocalDate().plusDays(1).atStartOfDay()))
                .sorted(Comparator.comparing(Purchase::getTimestamp))
                .collect(Collectors.toList());

        long countPurchases = recentPurchases.size();
        double totalAmountPurchases = recentPurchases.stream().mapToDouble(Purchase::getTotalValue).sum();

        double avg = countPurchases > 0 ? totalAmountPurchases / countPurchases : 0;

        return new DailyPurchasesStats(
                date,
                countPurchases,
                totalAmountPurchases,
                avg,
                recentPurchases.stream().mapToDouble(Purchase::getTotalValue).min().orElse(0.0),
                recentPurchases.stream().mapToDouble(Purchase::getTotalValue).max().orElse(0.0)
        );
    }
}
