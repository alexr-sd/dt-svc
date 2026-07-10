package com.logistics.importer;

import java.io.InputStream;
import java.util.List;

public interface DriverImporter {

    /**
     * Parses driver records from the given input stream.
     * Returns all valid rows; invalid rows are collected in {@link ParseResult#errors()}.
     *
     * @throws InvalidCsvException if the stream is unreadable or structurally invalid
     */
    ParseResult parse(InputStream input) throws InvalidCsvException;
}
