package com.logistics.dto;

import com.logistics.entity.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DeliveryEventRequest(
        @NotBlank String packageId,
        @NotBlank String driverId,
        @NotNull DeliveryStatus status,
        @NotNull LocalDateTime timestamp
) {
}
