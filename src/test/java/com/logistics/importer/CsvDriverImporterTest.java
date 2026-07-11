package com.logistics.importer;

import com.logistics.entity.Region;
import com.logistics.importer.driver.CsvDriverImporter;
import com.logistics.importer.driver.ParseResult;
import com.logistics.importer.driver.DriverRecord;
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
                D1,John,555-0001,john@logistics.com,north
                D2,Bob,555-0002,bob@logistics.com,SOUTH
                """);

        ParseResult result = importer.parse(input);

        assertThat(result.records()).hasSize(2);
        assertThat(result.errors()).isEmpty();

        DriverRecord first = result.records().getFirst();
        assertThat(first.driverId()).isEqualTo("D1");
        assertThat(first.name()).isEqualTo("John");
        assertThat(first.region()).isEqualTo(Region.NORTH);
    }

    @Test
    void collectsErrorForMissingDriverId() {
        ParseResult result = importer.parse(csv(",John,555,john@logistics.com,north\n"));

        assertThat(result.records()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst()).contains("driverId is required");
    }

    @Test
    void collectsErrorForMissingName() {
        ParseResult result = importer.parse(csv("D1,,555,andy@logistics.com,north\n"));

        assertThat(result.records()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst()).contains("name is required");
    }

    @Test
    void collectsErrorForInvalidRegion() {
        ParseResult result = importer.parse(csv("D1,Bill,555,bill@logistics.com,northwest\n"));

        assertThat(result.records()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst()).contains("invalid region").contains("northwest");
    }

    @Test
    void continuesParsingAfterBadRow() {
        InputStream input = csv("""
                D1,Andy,555,andy@logistics.com,north
                ,Missing Id,,,east
                D3,Carol,555,carol@logistics.com,west
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
        ParseResult result = importer.parse(csv("D1,Jim,,,east\n"));

        assertThat(result.records()).hasSize(1);
        assertThat(result.records().getFirst().phone()).isEmpty();
        assertThat(result.records().getFirst().email()).isEmpty();
    }
}
