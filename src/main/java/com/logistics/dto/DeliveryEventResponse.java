package com.logistics.dto;

import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryEventResponse(
        Long id,
        String packageId,
        String driverId,
        DeliveryStatus status,
        LocalDateTime timestamp
) {

    public static DeliveryEventResponse from(DeliveryEvent event) {
        return new DeliveryEventResponse(
                event.getId(),
                event.getPackageId(),
                event.getDriver().getDriverId(),
                event.getStatus(),
                event.getTimestamp()
        );
    }
}
