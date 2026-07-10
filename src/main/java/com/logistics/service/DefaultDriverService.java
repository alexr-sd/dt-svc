package com.logistics.service;

import com.logistics.dto.UploadResult;
import com.logistics.entity.Driver;
import com.logistics.importer.driver.DriverImporter;
import com.logistics.importer.driver.DriverRecord;
import com.logistics.importer.driver.InvalidCsvException;
import com.logistics.importer.driver.ParseResult;
import com.logistics.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
class DefaultDriverService implements DriverService {

    private final DriverImporter importer;
    private final DriverRepository repository;

    DefaultDriverService(DriverImporter importer, DriverRepository repository) {
        this.importer = importer;
        this.repository = repository;
    }

    @Override
    @Transactional
    public UploadResult importDrivers(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidCsvException("Uploaded file is empty");
        }

        ParseResult parsed;
        try {
            parsed = importer.parse(file.getInputStream());
        } catch (IOException e) {
            throw new InvalidCsvException("Could not read uploaded file", e);
        }

        List<Driver> drivers = parsed.records().stream()
                .map(this::toDriver)
                .toList();

        repository.saveAll(drivers);

        return new UploadResult(drivers.size(), parsed.errors().size(), parsed.errors());
    }

    private Driver toDriver(DriverRecord r) {
        return new Driver(r.driverId(), r.name(), r.phone(), r.email(), r.region());
    }
}
