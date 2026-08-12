package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.CsvFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvImportServiceTest {

    private TransportDataStore dataStore;
    private CsvImportService csvImportService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        dataStore = new TransportDataStore();
        csvImportService = new CsvImportService(dataStore);
    }

    @Test
    void validCsvImportsAllRows() throws Exception {
        Path csv = tempDir.resolve("valid.csv");
        Files.writeString(csv, "P-100,Test Pupil,07700-000000,SCH-01,R-102\n");

        int count = csvImportService.importPupilsFromCsv(csv.toString());

        assertEquals(1, count);
        assertNotNull(dataStore.findPupil("P-100"));
    }

    @Test
    void blankRequiredFieldThrowsCsvFormatException() throws IOException {
        Path csv = tempDir.resolve("blank_name.csv");
        Files.writeString(csv, "P-101,,07700-000000,SCH-01,R-102\n");

        assertThrows(CsvFormatException.class,
                () -> csvImportService.importPupilsFromCsv(csv.toString()));
    }

    @Test
    void wrongColumnCountThrowsCsvFormatException() throws IOException {
        Path csv = tempDir.resolve("bad_columns.csv");
        Files.writeString(csv, "P-102,Test Pupil,07700-000000\n"); // only 3 columns

        assertThrows(CsvFormatException.class,
                () -> csvImportService.importPupilsFromCsv(csv.toString()));
    }

    @Test
    void unknownRouteThrowsCsvFormatException() throws IOException {
        Path csv = tempDir.resolve("bad_route.csv");
        Files.writeString(csv, "P-103,Test Pupil,07700-000000,SCH-01,R-999\n");

        assertThrows(CsvFormatException.class,
                () -> csvImportService.importPupilsFromCsv(csv.toString()));
    }

    @Test
    void missingFileThrowsIOException() {
        assertThrows(IOException.class,
                () -> csvImportService.importPupilsFromCsv(tempDir.resolve("does-not-exist.csv").toString()));
    }
}
