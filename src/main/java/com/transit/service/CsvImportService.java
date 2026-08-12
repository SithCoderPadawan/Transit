package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.CsvFormatException;
import com.transit.exception.RouteCapacityException;
import com.transit.model.Pupil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Implements requirement 1a: "Entry of the data from the different
 * feeder schools (CSV format) into the system." Reads a CSV file of
 * pupil records and adds each one to the shared TransportDataStore,
 * assigning them to an existing route.
 *
 * Expected CSV format (no header row assumed to keep parsing simple
 * for the prototype):
 *   pupilId,name,emergencyContact,schoolId,routeId
 *
 * This class demonstrates handling a genuine Java API exception
 * (IOException, from file reading) alongside a custom exception
 * (CsvFormatException, for malformed rows) -- covering both halves
 * of the assignment's exception-handling requirement in one place.
 */
public class CsvImportService {

    private static final int EXPECTED_COLUMNS = 5;

    private final TransportDataStore dataStore;

    public CsvImportService(TransportDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Imports all pupils from the given CSV file.
     *
     * @return the number of pupils successfully imported
     * @throws IOException         if the file cannot be read (Java API exception)
     * @throws CsvFormatException  if any row is malformed (custom exception)
     * @throws RouteCapacityException if a row's target route is already full
     */
    public int importPupilsFromCsv(String filePath) throws IOException, CsvFormatException, RouteCapacityException {
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue; // tolerate blank lines without failing the whole import
                }

                Pupil pupil = parseLine(line, lineNumber);
                String routeId = line.split(",", -1)[4].trim();

                if (dataStore.findRoute(routeId) == null) {
                    throw new CsvFormatException(lineNumber, "unknown route id '" + routeId + "'");
                }

                dataStore.addPupilToRoute(pupil, routeId);
                imported++;
            }
        }
        // IOException from FileReader/BufferedReader is deliberately
        // NOT caught here -- it propagates to the caller (ConsoleMenu),
        // which is responsible for presenting it to the user. This
        // keeps the service layer honest about failure rather than
        // swallowing an I/O problem silently.

        return imported;
    }

    private Pupil parseLine(String line, int lineNumber) throws CsvFormatException {
        String[] fields = line.split(",", -1); // -1 keeps trailing empty fields, so blanks are caught below

        if (fields.length != EXPECTED_COLUMNS) {
            throw new CsvFormatException(lineNumber,
                    "expected " + EXPECTED_COLUMNS + " columns, found " + fields.length);
        }

        String pupilId = fields[0].trim();
        String name = fields[1].trim();
        String emergencyContact = fields[2].trim();
        String schoolId = fields[3].trim();
        String routeId = fields[4].trim();

        if (pupilId.isEmpty()) throw new CsvFormatException(lineNumber, "pupil id is blank");
        if (name.isEmpty()) throw new CsvFormatException(lineNumber, "pupil name is blank");
        if (schoolId.isEmpty()) throw new CsvFormatException(lineNumber, "school id is blank");
        if (routeId.isEmpty()) throw new CsvFormatException(lineNumber, "route id is blank");

        if (dataStore.findSchool(schoolId) == null) {
            throw new CsvFormatException(lineNumber, "unknown school id '" + schoolId + "'");
        }

        return new Pupil(pupilId, name, emergencyContact, schoolId, null);
    }
}
