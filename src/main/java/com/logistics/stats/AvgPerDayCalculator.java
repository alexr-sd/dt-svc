package com.logistics.stats;

import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.DeliveryStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AvgPerDayCalculator implements MetricCalculator {

    @Override
    public MetricType supports() {
        return MetricType.AVG_PER_DAY;
    }

    @Override
    public double calculate(List<DeliveryEvent> terminalEvents, long daysInRange) {
        if (daysInRange == 0) return 0.0;
        long delivered = terminalEvents.stream()
                .filter(e -> e.getStatus() == DeliveryStatus.DELIVERED)
                .count();
        return (double) delivered / daysInRange;
    }
}
