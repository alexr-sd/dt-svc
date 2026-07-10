package com.logistics.stats;

import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.DeliveryStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryRateCalculator implements MetricCalculator {

    @Override
    public MetricType supports() {
        return MetricType.DELIVERY_RATE;
    }

    @Override
    public double calculate(List<DeliveryEvent> terminalEvents, long daysInRange) {
        if (terminalEvents.isEmpty()) return 0.0;
        long delivered = terminalEvents.stream()
                .filter(e -> e.getStatus() == DeliveryStatus.DELIVERED)
                .count();
        return (double) delivered / terminalEvents.size();
    }
}
