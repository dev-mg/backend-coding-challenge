package com.journi.challenge.repositories;

import com.journi.challenge.models.DailyPurchasesStats;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Named
@Singleton
public class DailyPurchasesStatsRepository {

    private final List<DailyPurchasesStats> list = new ArrayList<>();

    public List<DailyPurchasesStats> list() {
        return list;
    }

    public void save(DailyPurchasesStats stats) {
        list.add(stats);
    }
}
