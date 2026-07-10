package com.logistics.controller;

import com.logistics.dto.StatsResponse;
import com.logistics.entity.Region;
import com.logistics.service.StatsService;
import com.logistics.stats.MetricType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
class StatsController {

    private final StatsService statsService;

    StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(
            @RequestParam(required = false) List<String> driverIds,
            @RequestParam(required = false) List<Region> regions,
            @RequestParam MetricType metric,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now();
        LocalDate effectiveTo   = to   != null ? to   : LocalDate.now();

        return ResponseEntity.ok(statsService.getStats(driverIds, regions, metric, effectiveFrom, effectiveTo));
    }
}
