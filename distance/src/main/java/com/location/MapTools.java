package com.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Converts an Address into latitude and longitude coordinates. */
public class MapTools {
    private static final String BASE_URL =
            "https://api.openrouteservice.org/geocode/search";

    private static final String DIRECTIONS_URL =
        "https://api.openrouteservice.org/v2/directions/driving-car";

    private static MapTools instance;

    // The API key comes from your .env file / environment variable.
    private final String apiKey = System.getenv("MAP_API_KEY");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private MapTools() {
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        objectMapper = new ObjectMapper();
    }

    public static MapTools getInstance() {
        if (instance == null) {
            instance = new MapTools();
        }

        return instance;
    }

    /**
     * Converts a street address into latitude and longitude.
     */
    public Coordinates getCoordinates(Address address)
            throws IOException, InterruptedException {

        checkForApiKey();

        String encodedAddress = URLEncoder.encode(
                getConversionString(address),
                StandardCharsets.UTF_8
        );

        URI uri = URI.create(
                BASE_URL
                        + "?api_key=" + apiKey
                        + "&text=" + encodedAddress
                        + "&boundary.country=USA"
                        + "&size=10"
        );

        HttpRequest request = HttpRequest.newBuilder(uri)
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException("Could not convert the address.");
        }

        JsonNode features = objectMapper
                .readTree(response.body())
                .path("features");

        JsonNode bestResult = findBestResult(features);

        return getCoordinates(bestResult);
    }

    /*
     * Prefer a real house-address result.
     * If one is not available, use the API's top match.
     */
    private JsonNode findBestResult(JsonNode features) throws IOException {
        if (!features.isArray() || features.isEmpty()) {
            throw new IOException("No location was found.");
        }

        for (JsonNode result : features) {
            String type = result.path("properties").path("layer").asText();

            if (type.equals("address")) {
                return result;
            }
        }

        return features.get(0);
    }

    /*
     * ORS returns coordinates in this order:
     * [longitude, latitude]
     */
    private Coordinates getCoordinates(JsonNode result) throws IOException {
        JsonNode coordinates = result.path("geometry").path("coordinates");

        if (!coordinates.isArray() || coordinates.size() < 2) {
            throw new IOException("Coordinates were missing.");
        }

        double longitude = coordinates.get(0).asDouble();
        double latitude = coordinates.get(1).asDouble();

        return new Coordinates(latitude, longitude);
    }

    private void checkForApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "MAP_API_KEY environment variable has not been set."
            );
        }
    }

    private String getConversionString(Address address){
        String street = address.getStreet();
        String city = address.getCity();
        String state = address.getState();
        String zip = address.getZip();
        return String.format("%s, %s, %s %s", street, city, state, zip);
    
    }

        /**
     * Returns the address with the shortest driving time from the starting address.
     */
    public Address findClosestAddress(Address startingAddress, Address[] addresses)
            throws IOException, InterruptedException {

        Coordinates start = getCoordinates(startingAddress);

        Address closestAddress = null;
        double shortestTime = Double.MAX_VALUE;

        for (Address address : addresses) {
            System.out.println("Checking shelter: " + address);

            Coordinates destination = getCoordinates(address);
            double drivingTime = getDrivingTime(start, destination);

            if (drivingTime < shortestTime) {
                shortestTime = drivingTime;
                closestAddress = address;
            }
        }

        return closestAddress;
    }

    /**
     * Returns driving time in seconds between two coordinate locations.
     */
    private double getDrivingTime(Coordinates start, Coordinates destination)
        throws IOException, InterruptedException {

        JsonNode route = getRoute(start, destination);

        return route.path("summary").path("duration").asDouble();
    }

    private JsonNode getRoute(Coordinates start, Coordinates end)
        throws IOException, InterruptedException {

        String requestBody =
                "{ \"coordinates\": [[" +
                start.getLongitude() + "," + start.getLatitude() +
                "],[" +
                end.getLongitude() + "," + end.getLatitude() +
                "]], \"instructions\": true, \"language\": \"en\" }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DIRECTIONS_URL))
                .header("Authorization", apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException("Could not get driving directions.");
        }

        return objectMapper
                .readTree(response.body())
                .path("routes")
                .path(0);
    }

    public String[] getDrivingDirections(Address startingAddress, Address endingAddress)
        throws IOException, InterruptedException {

        Coordinates start = getCoordinates(startingAddress);
        Coordinates end = getCoordinates(endingAddress);

        JsonNode route = getRoute(start, end);
        JsonNode steps = route.path("segments").path(0).path("steps");

        String[] directions = new String[steps.size()];

        for (int i = 0; i < steps.size(); i++) {
            directions[i] = steps.get(i)
                    .path("instruction")
                    .asText();
        }

        return directions;
    }
}