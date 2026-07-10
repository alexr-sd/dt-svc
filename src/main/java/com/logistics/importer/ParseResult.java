package com.logistics.importer;

import java.util.List;

public record ParseResult(List<DriverRecord> records, List<String> errors) {
}
