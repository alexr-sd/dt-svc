package com.logistics.importer.driver;

import com.logistics.entity.Region;

public record DriverRecord(
        String driverId,
        String name,
        String phone,
        String email,
        Region region
) {
}
