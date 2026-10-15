package com.location;

public class GeocodeResult {
    private final Coordinates coordinates;
    private final String matchedAddress;
    private final String matchType;
    private final double confidence;

    public GeocodeResult(
            Coordinates coordinates,
            String matchedAddress,
            String matchType,
            double confidence) {

        this.coordinates = coordinates;
        this.matchedAddress = matchedAddress;
        this.matchType = matchType;
        this.confidence = confidence;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getMatchedAddress() {
        return matchedAddress;
    }

    public String getMatchType() {
        return matchType;
    }

    public double getConfidence() {
        return confidence;
    }
}