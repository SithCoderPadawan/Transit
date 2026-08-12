package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.InvalidRouteException;
import com.transit.exception.PupilNotFoundException;
import com.transit.exception.RouteCapacityException;
import com.transit.model.Pupil;
import com.transit.model.Route;

public class RouteManager {
    private final TransportDataStore dataStore;

    public RouteManager(TransportDataStore dataStore) { this.dataStore = dataStore; }

    public void movePupilToRoute(String pupilId, String newRouteId)
            throws PupilNotFoundException, InvalidRouteException, RouteCapacityException {

        Pupil pupil = dataStore.findPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);

        Route newRoute = dataStore.findRoute(newRouteId);
        if (newRoute == null) throw new InvalidRouteException(newRouteId);

        newRoute.addPupil(pupil);

        Route oldRoute = dataStore.findRoute(pupil.getRouteId());
        if (oldRoute != null) oldRoute.removePupil(pupil);

        pupil.setRouteId(newRouteId);
    }

    public Route getRouteForPupil(String pupilId) throws PupilNotFoundException, InvalidRouteException {
        Pupil pupil = dataStore.findPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);
        Route route = dataStore.findRoute(pupil.getRouteId());
        if (route == null) throw new InvalidRouteException(String.valueOf(pupil.getRouteId()));
        return route;
    }
}
