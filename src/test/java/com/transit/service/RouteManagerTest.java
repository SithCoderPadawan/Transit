package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.InvalidRouteException;
import com.transit.exception.PupilNotFoundException;
import com.transit.exception.RouteCapacityException;
import com.transit.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteManagerTest {

    private RouteManager routeManager;

    @BeforeEach
    void setUp() { routeManager = new RouteManager(new TransportDataStore()); }

    @Test
    void movingPupilIntoFullRouteThrowsRouteCapacityException() {
        assertThrows(RouteCapacityException.class,
                () -> routeManager.movePupilToRoute("P-003", "R-101"));
    }

    @Test
    void movingPupilToRouteWithSpaceSucceedsAndRemovesFromOldRoute() throws Exception {
        routeManager.movePupilToRoute("P-001", "R-102");
        Route newRoute = routeManager.getRouteForPupil("P-001");
        assertEquals("R-102", newRoute.getRouteId());

        Route oldRoute = routeManager.getRouteForPupil("P-002");
        assertEquals("R-101", oldRoute.getRouteId());
        assertFalse(oldRoute.getPupils().stream().anyMatch(p -> p.getPupilId().equals("P-001")));
    }

    @Test
    void movingUnknownPupilThrowsPupilNotFoundException() {
        assertThrows(PupilNotFoundException.class,
                () -> routeManager.movePupilToRoute("NO-SUCH-PUPIL", "R-102"));
    }

    @Test
    void movingToUnknownRouteThrowsInvalidRouteException() {
        assertThrows(InvalidRouteException.class,
                () -> routeManager.movePupilToRoute("P-003", "NO-SUCH-ROUTE"));
    }

    @Test
    void getRouteForPupilReturnsCorrectRoute() throws Exception {
        Route route = routeManager.getRouteForPupil("P-001");
        assertEquals("R-101", route.getRouteId());
    }
}
