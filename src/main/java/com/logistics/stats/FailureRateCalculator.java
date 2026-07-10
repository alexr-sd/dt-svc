package com.logistics.stats;

import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.DeliveryStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FailureRateCalculator implements MetricCalculator {

    @Override
    public MetricType supports() {
        return MetricType.FAILURE_RATE;
    }

    @Override
    public double calculate(List<DeliveryEvent> terminalEvents, long daysInRange) {
        if (terminalEvents.isEmpty()) return 0.0;
        long failed = terminalEvents.stream()
                .filter(e -> e.getStatus() == DeliveryStatus.FAILED
                          || e.getStatus() == DeliveryStatus.RETURNED)
                .count();
        return (double) failed / terminalEvents.size();
    }
}
