package com.logistics.importer;

import com.logistics.domain.Region;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CsvDriverImporterTest {

    private final CsvDriverImporter importer = new CsvDriverImporter();

    private static final String HEADER = "driverId,name,phone,email,region\n";

    private InputStream csv(String rows) {
        return new ByteArrayInputStream((HEADER + rows).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parsesValidRows() {
        InputStream input = csv("""
                D1,Alice,555-0001,alice@example.com,north
                D2,Bob,555-0002,bob@example.com,SOUTH
                """);

        ParseResult result = importer.parse(input);

        assertThat(result.records()).hasSize(2);
        assertThat(result.errors()).isEmpty();

        DriverRecord first = result.records().getFirst();
        assertThat(first.driverId()).isEqualTo("D1");
        assertThat(first.name()).isEqualTo("Alice");
        assertThat(first.region()).isEqualTo(Region.NORTH);
    }

    @Test
    void collectsErrorForMissingDriverId() {
        ParseResult result = importer.parse(csv(",Alice,555,alice@example.com,north\n"));

        assertThat(result.records()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst()).contains("driverId is required");
    }

    @Test
    void collectsErrorForMissingName() {
        ParseResult result = importer.parse(csv("D1,,555,alice@example.com,north\n"));

        assertThat(result.records()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst()).contains("name is required");
    }

    @Test
    void collectsErrorForInvalidRegion() {
        ParseResult result = importer.parse(csv("D1,Alice,555,alice@example.com,northwest\n"));

        assertThat(result.records()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst()).contains("invalid region").contains("northwest");
    }

    @Test
    void continuesParsingAfterBadRow() {
        InputStream input = csv("""
                D1,Alice,555,alice@example.com,north
                ,Missing Id,,,east
                D3,Carol,555,carol@example.com,west
                """);

        ParseResult result = importer.parse(input);

        assertThat(result.records()).hasSize(2);
        assertThat(result.errors()).hasSize(1);
    }

    @Test
    void emptyFileReturnsEmptyResult() {
        ParseResult result = importer.parse(csv(""));

        assertThat(result.records()).isEmpty();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void allowsEmptyOptionalFields() {
        ParseResult result = importer.parse(csv("D1,Alice,,,east\n"));

        assertThat(result.records()).hasSize(1);
        assertThat(result.records().getFirst().phone()).isEmpty();
        assertThat(result.records().getFirst().email()).isEmpty();
    }
}
