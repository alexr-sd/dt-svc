package com.logistics.importer.driver;

import java.util.List;

public record ParseResult(List<DriverRecord> records, List<String> errors) {
}
