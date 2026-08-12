package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.InvalidContractorException;
import com.transit.exception.InvalidRatingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContractServiceTest {

    private ContractService contractService;
    private TransportDataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new TransportDataStore();
        contractService = new ContractService(dataStore);
    }

    @Test
    void validReviewUpdatesPerformanceRating() throws Exception {
        contractService.reviewContractorPerformance("Valley Coaches", 2);
        assertEquals(2, dataStore.findContractor("Valley Coaches").getPerformanceRating());
    }

    @Test
    void unknownContractorThrowsInvalidContractorException() {
        assertThrows(InvalidContractorException.class,
                () -> contractService.reviewContractorPerformance("Ghost Coaches", 3));
    }

    @Test
    void ratingBelowMinimumThrowsInvalidRatingException() {
        assertThrows(InvalidRatingException.class,
                () -> contractService.reviewContractorPerformance("Valley Coaches", 0));
    }

    @Test
    void ratingAboveMaximumThrowsInvalidRatingException() {
        assertThrows(InvalidRatingException.class,
                () -> contractService.reviewContractorPerformance("Valley Coaches", 6));
    }

    @Test
    void listContractorsIncludesBusDetails() {
        String joined = String.join("\n", contractService.listContractors());
        assertTrue(joined.contains("Valley Coaches"));
        assertTrue(joined.contains("BUS-07"));
        assertTrue(joined.contains("BUS-12"));
    }
}
