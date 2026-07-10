package com.logistics.service;

import com.logistics.dto.StatsResponse;
import com.logistics.entity.Region;
import com.logistics.stats.MetricType;

import java.time.LocalDate;
import java.util.List;

public interface StatsService {

    StatsResponse getStats(
            List<String> driverIds,
            List<Region> regions,
            MetricType metric,
            LocalDate from,
            LocalDate to
    );
}
