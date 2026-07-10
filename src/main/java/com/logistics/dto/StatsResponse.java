package com.logistics.dto;

import java.util.List;
import java.util.Map;

public record StatsResponse(
        String metric,
        double value,
        Map<String, Object> filters,
        Map<String, String> dateRange
) {

    public static StatsResponse of(
            String metric,
            double value,
            List<String> driverIds,
            List<String> regions,
            String from,
            String to
    ) {
        return new StatsResponse(
                metric,
                value,
                Map.of(
                        "driverIds", driverIds == null ? List.of() : driverIds,
                        "regions",   regions   == null ? List.of() : regions
                ),
                Map.of("from", from, "to", to)
        );
    }
}
