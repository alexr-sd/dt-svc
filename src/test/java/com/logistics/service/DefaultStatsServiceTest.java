package com.logistics.service;

import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.DeliveryStatus;
import com.logistics.entity.Driver;
import com.logistics.entity.Region;
import com.logistics.repository.DeliveryEventRepository;
import com.logistics.stats.MetricType;
import com.logistics.stats.TotalPackagesCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultStatsServiceTest {

    @Mock
    private DeliveryEventRepository eventRepository;

    private DefaultStatsService service;

    private static final Driver NORTH_DRIVER = new Driver("d-north", "Andy", null, null, Region.NORTH);
    private static final Driver SOUTH_DRIVER = new Driver("d-south", "Bob",   null, null, Region.SOUTH);
    private static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new DefaultStatsService(eventRepository, List.of(new TotalPackagesCalculator()));
    }

    @Test
    void throwsWhenFromAfterTo() {
        var from = LocalDate.of(2024, 5, 10);
        var to = LocalDate.of(2024, 5, 1);
        assertThatThrownBy(() -> service.getStats(null, null, MetricType.TOTAL_PACKAGES, from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'from' must not be after 'to'");
    }

    @Test
    void throwsWhenRangeExceeds31Days() {
        var from = LocalDate.of(2024, 1, 1);
        var to = LocalDate.of(2024, 2, 10);
        assertThatThrownBy(() -> service.getStats(null, null, MetricType.TOTAL_PACKAGES, from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("31");
    }

    @Test
    void filtersEventsByDriverId() {
        var northEvent = new DeliveryEvent("pkg-1", NORTH_DRIVER, DeliveryStatus.DELIVERED, NOW);
        var southEvent = new DeliveryEvent("pkg-2", SOUTH_DRIVER, DeliveryStatus.DELIVERED, NOW);
        when(eventRepository.findTerminalEventsInRange(any(), any())).thenReturn(List.of(northEvent, southEvent));

        var result = service.getStats(
                List.of("d-north"), null, MetricType.TOTAL_PACKAGES,
                LocalDate.now(), LocalDate.now()
        );

        assertThat(result.value()).isEqualTo(1.0);
    }

    @Test
    void filtersEventsByRegion() {
        var northEvent = new DeliveryEvent("pkg-1", NORTH_DRIVER, DeliveryStatus.DELIVERED, NOW);
        var southEvent = new DeliveryEvent("pkg-2", SOUTH_DRIVER, DeliveryStatus.DELIVERED, NOW);
        when(eventRepository.findTerminalEventsInRange(any(), any())).thenReturn(List.of(northEvent, southEvent));

        var result = service.getStats(
                null, List.of(Region.SOUTH), MetricType.TOTAL_PACKAGES,
                LocalDate.now(), LocalDate.now()
        );

        assertThat(result.value()).isEqualTo(1.0);
    }

    @Test
    void noFilters_returnsAllEvents() {
        when(eventRepository.findTerminalEventsInRange(any(), any())).thenReturn(List.of(
                new DeliveryEvent("pkg-1", NORTH_DRIVER, DeliveryStatus.DELIVERED, NOW),
                new DeliveryEvent("pkg-2", SOUTH_DRIVER, DeliveryStatus.FAILED, NOW)
        ));

        var result = service.getStats(null, null, MetricType.TOTAL_PACKAGES,
                LocalDate.now(), LocalDate.now());

        assertThat(result.value()).isEqualTo(2.0);
    }
}
