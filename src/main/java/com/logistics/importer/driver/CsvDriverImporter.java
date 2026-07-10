package com.logistics.importer.driver;

import com.logistics.entity.Region;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvDriverImporter implements DriverImporter {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("driverId", "name", "phone", "email", "region")
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    @Override
    public ParseResult parse(InputStream input) throws InvalidCsvException {
        List<DriverRecord> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (CSVParser parser = FORMAT.parse(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            int rowNumber = 1;
            for (CSVRecord row : parser) {
                rowNumber++;
                try {
                    records.add(toRecord(row));
                } catch (IllegalArgumentException e) {
                    errors.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new InvalidCsvException("Failed to read CSV: " + e.getMessage(), e);
        }

        return new ParseResult(records, errors);
    }

    private DriverRecord toRecord(CSVRecord row) {
        String driverId  = row.get("driverId");
        String name      = row.get("name");
        String phone     = row.get("phone");
        String email     = row.get("email");
        String regionRaw = row.get("region");

        if (driverId == null || driverId.isBlank())  throw new IllegalArgumentException("driverId is required");
        if (name     == null || name.isBlank())       throw new IllegalArgumentException("name is required");
        if (regionRaw == null || regionRaw.isBlank()) throw new IllegalArgumentException("region is required");

        Region region;
        try {
            region = Region.valueOf(regionRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid region: '" + regionRaw + "'");
        }

        return new DriverRecord(driverId, name, phone, email, region);
    }
}
