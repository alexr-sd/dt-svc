package com.logistics.importer.driver;

import java.io.InputStream;

public interface DriverImporter {

    ParseResult parse(InputStream input) throws InvalidCsvException;
}
