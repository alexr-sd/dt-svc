package com.logistics.service;

import com.logistics.dto.StatsResponse;
import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.Region;
import com.logistics.repository.DeliveryEventRepository;
import com.logistics.stats.MetricCalculator;
import com.logistics.stats.MetricType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class DefaultStatsService implements StatsService {

    private static final int MAX_RANGE_DAYS = 31;

    private final DeliveryEventRepository eventRepository;
    private final Map<MetricType, MetricCalculator> calculators;

    DefaultStatsService(DeliveryEventRepository eventRepository, List<MetricCalculator> calculators) {
        this.eventRepository = eventRepository;
        this.calculators = calculators.stream()
                .collect(Collectors.toMap(MetricCalculator::supports, Function.identity()));
    }

    @Override
    public StatsResponse getStats(
            List<String> driverIds,
            List<Region> regions,
            MetricType metric,
            LocalDate from,
            LocalDate to
    ) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        long daysInRange = ChronoUnit.DAYS.between(from, to) + 1;
        if (daysInRange > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("Date range must not exceed " + MAX_RANGE_DAYS + " days");
        }

        List<DeliveryEvent> events = eventRepository.findTerminalEventsInRange(
                from.atStartOfDay(),
                to.atTime(LocalTime.MAX)
        );

        List<DeliveryEvent> filtered = events.stream()
                .filter(e -> driverIds == null || driverIds.isEmpty() || driverIds.contains(e.getDriver().getDriverId()))
                .filter(e -> regions   == null || regions.isEmpty()   || regions.contains(e.getDriver().getRegion()))
                .toList();

        double value = calculators.get(metric).calculate(filtered, daysInRange);

        return StatsResponse.of(
                metric.name().toLowerCase(),
                value,
                driverIds,
                regions == null ? null : regions.stream().map(Region::name).map(String::toLowerCase).toList(),
                from.toString(),
                to.toString()
        );
    }
}
