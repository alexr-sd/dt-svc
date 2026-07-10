package com.logistics.stats;

import com.logistics.entity.DeliveryEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TotalPackagesCalculator implements MetricCalculator {

    @Override
    public MetricType supports() {
        return MetricType.TOTAL_PACKAGES;
    }

    @Override
    public double calculate(List<DeliveryEvent> terminalEvents, long daysInRange) {
        return terminalEvents.size();
    }
}
