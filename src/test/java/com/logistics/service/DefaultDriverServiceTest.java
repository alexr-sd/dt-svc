package com.logistics.service;

import com.logistics.entity.Driver;
import com.logistics.entity.Region;
import com.logistics.importer.driver.DriverImporter;
import com.logistics.importer.driver.DriverRecord;
import com.logistics.importer.driver.InvalidCsvException;
import com.logistics.importer.driver.ParseResult;
import com.logistics.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultDriverServiceTest {

    @Mock
    private DriverImporter importer;

    @Mock
    private DriverRepository repository;

    @InjectMocks
    private DefaultDriverService service;

    @Test
    void throwsWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> service.importDrivers(file))
                .isInstanceOf(InvalidCsvException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void importsAllValidRows() {
        MockMultipartFile file = new MockMultipartFile("file", "drivers.csv", "text/csv", "data".getBytes());

        List<DriverRecord> records = List.of(
                new DriverRecord("D1", "Jim", "555", "jim@mail.com", Region.NORTH),
                new DriverRecord("D2", "Pat",   "556", "pat@mail.com",   Region.SOUTH)
        );
        when(importer.parse(any(InputStream.class))).thenReturn(new ParseResult(records, List.of()));

        var result = service.importDrivers(file);

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.skipped()).isZero();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void savesCorrectlyMappedDriverEntities() {
        MockMultipartFile file = new MockMultipartFile("file", "drivers.csv", "text/csv", "data".getBytes());

        List<DriverRecord> records = List.of(
                new DriverRecord("D1", "Henry", "555", "henry@mail.com", Region.NORTH)
        );
        when(importer.parse(any(InputStream.class))).thenReturn(new ParseResult(records, List.of()));

        service.importDrivers(file);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Driver>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());

        Driver saved = captor.getValue().getFirst();
        assertThat(saved.getDriverId()).isEqualTo("D1");
        assertThat(saved.getName()).isEqualTo("Henry");
        assertThat(saved.getRegion()).isEqualTo(Region.NORTH);
    }

    @Test
    void returnsPartialResultWhenSomeRowsFail() {
        MockMultipartFile file = new MockMultipartFile("file", "drivers.csv", "text/csv", "data".getBytes());

        List<DriverRecord> records = List.of(
                new DriverRecord("D1", "Bon", null, null, Region.EAST)
        );
        List<String> errors = List.of("Row 3: driverId is required");
        when(importer.parse(any(InputStream.class))).thenReturn(new ParseResult(records, errors));

        var result = service.importDrivers(file);

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.errors()).containsExactly("Row 3: driverId is required");
    }
}
