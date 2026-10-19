package com.location;

/*
 * Represents one address after it has been converted
 * into latitude and longitude coordinates.
 */
public class MapLocation {

    private final Address address;
    private final Coordinates coordinates;

    public MapLocation(Address address, Coordinates coordinates) {
        this.address = address;
        this.coordinates = coordinates;
    }

    public Address getAddress() {
        return address;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return address.toString();
    }
}