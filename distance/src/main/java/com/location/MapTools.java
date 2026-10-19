package com.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 
 * MapTools Provides map api tools
 */
public class MapTools {
    private static final String GEOCODING_URL =
        "https://api.heigit.org/pelias/v1/search";

    private static final String DIRECTIONS_URL =
        "https://api.heigit.org/openrouteservice/v2/directions/driving-car";

    private static final String ROUTE_GEOJSON_URL =
        "https://api.heigit.org/openrouteservice/v2/directions/driving-car/geojson";
    private static MapTools mapTools;
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
        if(mapTools == null){
            mapTools = new MapTools();
        }
        return mapTools;
    }

    /**
     * Converts a street address into latitude and longitude.
     * @param address Street address
     * @return Coordinates in latitude and longitude
     * @throws IOException
     * @throws InterruptedException
     */
    public Coordinates getCoordinates(Address address)
            throws IOException, InterruptedException {

        checkForApiKey();

        String encodedAddress = URLEncoder.encode(
                address.toString(),
                StandardCharsets.UTF_8
        );

        URI uri = URI.create(
                GEOCODING_URL
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
            throw new IOException(
                    "Could not convert: " + address
                            + " (HTTP " + response.statusCode() + ")"
                            + "\n" + response.body()
            );
        }

        JsonNode features = objectMapper
                .readTree(response.body())
                .path("features");

        JsonNode bestResult = findBestResult(features);

        return readCoordinates(bestResult);
    }

    /**
     * Finds the shelter with the shortest driving time.
     * @param startingAddress The address we are driving from
     * @param shelters The list of shelters we are choosing from
     * @return The address of the closest shelter
     * @throws IOException
     * @throws InterruptedException
     */
    public Address findClosestAddress(Address startingAddress, Address[] shelters)
            throws IOException, InterruptedException {

        Coordinates startingCoordinates = getCoordinates(startingAddress);

        Address closestShelter = null;
        double shortestTime = Double.MAX_VALUE;

        for (Address shelter : shelters) {
            Coordinates shelterCoordinates = getCoordinates(shelter);

            double drivingTime = getDrivingTime(
                    startingCoordinates,
                    shelterCoordinates
            );

            if (drivingTime < shortestTime) {
                shortestTime = drivingTime;
                closestShelter = shelter;
            }
        }

        return closestShelter;
    }

    /**
     * Collects the driving directions to the shelter
     * @param startingAddress The address the route starts at
     * @param endingAddress The address the route ends at
     * @return A string array where each item represents a direction on the route
     * @throws IOException
     * @throws InterruptedException
     */
    public String[] getDrivingDirections(
            Address startingAddress,
            Address endingAddress
    ) throws IOException, InterruptedException {

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

    /**
     * Finds the best version of a coordinate. Based on the address we might not 
     * be able to find the exact house, but maybe the city
     * @param featuresThe information about the street address
     * @return The node representation of the closest address
     * @throws IOException
     */
    private JsonNode findBestResult(JsonNode features) throws IOException {
        if (!features.isArray() || features.isEmpty()) {
            throw new IOException("No location was found.");
        }

        for (JsonNode result : features) {
            String layer = result.path("properties").path("layer").asText();

            if (layer.equals("address")) {
                return result;
            }
        }

        return features.get(0);
    }

    /**
     * Covert a node into a coordinate object
     * @param result The node representing the location
     * @return The coordinates of the node in latitude and longitude
     * @throws IOException
     */
    private Coordinates readCoordinates(JsonNode result) throws IOException {
        JsonNode coordinates = result.path("geometry").path("coordinates");

        if (!coordinates.isArray() || coordinates.size() < 2) {
            throw new IOException("Coordinates were missing.");
        }

        double longitude = coordinates.get(0).asDouble();
        double latitude = coordinates.get(1).asDouble();

        return new Coordinates(latitude, longitude);
    }

    /**
     * Calculates the driving time in seconds between two coordinates
     * @param start Starting location
     * @param end Ending location
     * @return The number of seconds
     * @throws IOException
     * @throws InterruptedException
     */
    private double getDrivingTime(Coordinates start, Coordinates end)
            throws IOException, InterruptedException {

        JsonNode route = getRoute(start, end);

        return route.path("summary").path("duration").asDouble();
    }

    /**
     * Calculates the route information from one location to the next
     * @param start Starting location
     * @param end Ending location
     * @return A json node representing the route.
     * @throws IOException
     * @throws InterruptedException
     */
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

    /**
     * Determines the route from one address to another and returns it to show on a map.
     * @param startingAddress   The address the route begins at
     * @param endingAddress The address the route ends on
     * @return The Driving route to be plotted on a map
     * @throws IOException
     * @throws InterruptedException
     */
    public DrivingRoute getDrivingRoute(Address startingAddress, Address endingAddress) 
        throws IOException, InterruptedException {

        Coordinates startCoordinates = getCoordinates(startingAddress);
        Coordinates endCoordinates = getCoordinates(endingAddress);

        ArrayList<Coordinates> routePoints = getRoutePoints(
                startCoordinates,
                endCoordinates
        );

        MapLocation startingLocation = new MapLocation(
                startingAddress,
                startCoordinates
        );

        MapLocation endingLocation = new MapLocation(
                endingAddress,
                endCoordinates
        );

        return new DrivingRoute(
                startingLocation,
                endingLocation,
                routePoints
        );
    }

    /**
     * Determines all the points to show on the route to be plotted on a map
     * @param start The address to begin on as a coordinate
     * @param end The address to end at as a coordinate
     * @return A list of all points to be put on the route
     * @throws IOException
     * @throws InterruptedException
     */
    private ArrayList<Coordinates> getRoutePoints(Coordinates start,Coordinates end
    ) throws IOException, InterruptedException {

        String requestBody =
                "{ \"coordinates\": [[" +
                        start.getLongitude() + "," + start.getLatitude() +
                        "],[" +
                        end.getLongitude() + "," + end.getLatitude() +
                        "]]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ROUTE_GEOJSON_URL))
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
            throw new IOException("Could not get the driving route.");
        }

        JsonNode points = objectMapper
                .readTree(response.body())
                .path("features")
                .path(0)
                .path("geometry")
                .path("coordinates");

        ArrayList<Coordinates> routePoints = new ArrayList<>();

        for (JsonNode point : points) {
            double longitude = point.get(0).asDouble();
            double latitude = point.get(1).asDouble();

            routePoints.add(new Coordinates(latitude, longitude));
        }

        return routePoints;
    }

    /**
     * Determine if we have a valid API key in our environment variable.
     */
    private void checkForApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "MAP_API_KEY environment variable has not been set."
            );
        }
    }
}