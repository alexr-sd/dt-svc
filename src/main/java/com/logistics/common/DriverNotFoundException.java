package com.logistics.common;

public class DriverNotFoundException extends RuntimeException {

    public DriverNotFoundException(String driverId) {
        super("Driver not found: " + driverId);
    }
}
