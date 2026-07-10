package com.logistics.stats;

import com.logistics.entity.DeliveryEvent;

import java.util.List;

public interface MetricCalculator {

    MetricType supports();

    double calculate(List<DeliveryEvent> terminalEvents, long daysInRange);
}
