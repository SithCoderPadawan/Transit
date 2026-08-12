package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.InvalidContractorException;
import com.transit.exception.InvalidRatingException;
import com.transit.model.Bus;
import com.transit.model.BusContractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the "Manage bus contracts" use case -- LEA-only, per
 * requirement 2c ("LEA can change everything that schools can
 * change... but in addition they can change bus contracts").
 *
 * Corresponds to the case study's June review cycle: the LEA reviews
 * each contractor's performance and contacts those with low ratings.
 * This service provides the reviewing/rating half of that workflow;
 * the ITT/tendering process itself is out of scope for the prototype.
 */
public class ContractService {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final TransportDataStore dataStore;

    public ContractService(TransportDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void reviewContractorPerformance(String companyName, int newRating)
            throws InvalidContractorException, InvalidRatingException {

        if (newRating < MIN_RATING || newRating > MAX_RATING) {
            throw new InvalidRatingException(newRating);
        }

        BusContractor contractor = dataStore.findContractor(companyName);
        if (contractor == null) {
            throw new InvalidContractorException(companyName);
        }

        contractor.reviewPerformance(newRating);
    }

    /** Read-only listing, used by the console menu to show contractors before reviewing one. */
    public List<String> listContractors() {
        List<String> lines = new ArrayList<>();
        for (BusContractor contractor : dataStore.getAllContractors().values()) {
            lines.add(String.format("%s - current rating %d/5, %d bus(es)",
                    contractor.getCompanyName(), contractor.getPerformanceRating(), contractor.getBuses().size()));
            for (Bus bus : contractor.getBuses()) {
                lines.add("    bus " + bus.getRegPlate() + " (capacity " + bus.getCapacity() + ")");
            }
        }
        return lines;
    }
}
