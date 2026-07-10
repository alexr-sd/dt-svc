package com.logistics.stats;

import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.DeliveryStatus;
import com.logistics.entity.Driver;
import com.logistics.entity.Region;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class MetricCalculatorTest {

    private static final Driver DRIVER = new Driver("d1", "Alice", null, null, Region.NORTH);
    private static final LocalDateTime NOW = LocalDateTime.now();

    private static DeliveryEvent event(DeliveryStatus status) {
        return new DeliveryEvent("pkg-1", DRIVER, status, NOW);
    }

    // TotalPackagesCalculator

    @Test
    void totalPackages_returnsListSize() {
        var calc = new TotalPackagesCalculator();
        assertThat(calc.calculate(List.of(event(DeliveryStatus.DELIVERED), event(DeliveryStatus.FAILED)), 7))
                .isEqualTo(2.0);
    }

    @Test
    void totalPackages_emptyList_returnsZero() {
        assertThat(new TotalPackagesCalculator().calculate(List.of(), 7)).isEqualTo(0.0);
    }

    // DeliveryRateCalculator

    @Test
    void deliveryRate_allDelivered_returnsOne() {
        var calc = new DeliveryRateCalculator();
        assertThat(calc.calculate(List.of(event(DeliveryStatus.DELIVERED), event(DeliveryStatus.DELIVERED)), 7))
                .isEqualTo(1.0, offset(0.001));
    }

    @Test
    void deliveryRate_mixed_returnsRatio() {
        var calc = new DeliveryRateCalculator();
        var events = List.of(
                event(DeliveryStatus.DELIVERED),
                event(DeliveryStatus.FAILED),
                event(DeliveryStatus.RETURNED)
        );
        assertThat(calc.calculate(events, 7)).isEqualTo(1.0 / 3.0, offset(0.001));
    }

    @Test
    void deliveryRate_emptyList_returnsZero() {
        assertThat(new DeliveryRateCalculator().calculate(List.of(), 7)).isEqualTo(0.0);
    }

    // FailureRateCalculator

    @Test
    void failureRate_failedAndReturnedBothCount() {
        var calc = new FailureRateCalculator();
        var events = List.of(
                event(DeliveryStatus.DELIVERED),
                event(DeliveryStatus.FAILED),
                event(DeliveryStatus.RETURNED)
        );
        assertThat(calc.calculate(events, 7)).isEqualTo(2.0 / 3.0, offset(0.001));
    }

    @Test
    void failureRate_noneFailedOrReturned_returnsZero() {
        var calc = new FailureRateCalculator();
        assertThat(calc.calculate(List.of(event(DeliveryStatus.DELIVERED)), 7)).isEqualTo(0.0);
    }

    @Test
    void failureRate_emptyList_returnsZero() {
        assertThat(new FailureRateCalculator().calculate(List.of(), 7)).isEqualTo(0.0);
    }

    // AvgPerDayCalculator

    @Test
    void avgPerDay_dividesDeliveredByDays() {
        var calc = new AvgPerDayCalculator();
        var events = List.of(
                event(DeliveryStatus.DELIVERED),
                event(DeliveryStatus.DELIVERED),
                event(DeliveryStatus.FAILED)
        );
        assertThat(calc.calculate(events, 2)).isEqualTo(1.0, offset(0.001));
    }

    @Test
    void avgPerDay_zeroDays_returnsZero() {
        var calc = new AvgPerDayCalculator();
        assertThat(calc.calculate(List.of(event(DeliveryStatus.DELIVERED)), 0)).isEqualTo(0.0);
    }

    @Test
    void avgPerDay_emptyList_returnsZero() {
        assertThat(new AvgPerDayCalculator().calculate(List.of(), 7)).isEqualTo(0.0);
    }
}
