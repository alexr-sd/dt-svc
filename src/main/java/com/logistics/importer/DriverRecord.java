package com.logistics.importer;

import com.logistics.domain.Region;

public record DriverRecord(
        String driverId,
        String name,
        String phone,
        String email,
        Region region
) {
}
