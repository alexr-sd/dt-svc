package com.logistics.common;

import com.logistics.stats.MetricType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMetricTypeConverter implements Converter<String, MetricType> {

    @Override
    public MetricType convert(String source) {
        try {
            return MetricType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid metric: '" + source + "'. Valid values: total_packages, delivery_rate, failure_rate, avg_per_day"
            );
        }
    }
}
