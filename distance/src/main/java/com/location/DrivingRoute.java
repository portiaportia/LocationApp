package com.location;

import java.util.ArrayList;

public class DrivingRoute {

    private final MapLocation startingLocation;
    private final MapLocation endingLocation;
    private final ArrayList<Coordinates> routePoints;

    public DrivingRoute(
            MapLocation startingLocation,
            MapLocation endingLocation,
            ArrayList<Coordinates> routePoints
    ) {
        this.startingLocation = startingLocation;
        this.endingLocation = endingLocation;
        this.routePoints = routePoints;
    }

    public MapLocation getStartingLocation() {
        return startingLocation;
    }

    public MapLocation getEndingLocation() {
        return endingLocation;
    }

    public ArrayList<Coordinates> getRoutePoints() {
        return routePoints;
    }
}