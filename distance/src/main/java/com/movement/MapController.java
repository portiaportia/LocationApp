package com.movement;

import java.util.ArrayList;
import com.location.*;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.util.Duration;

public class MapController {

    @FXML
    private WebView webView;
    private MapLocation[] locationsToShow;
    private ArrayList<Coordinates> routePointsToShow;

    @FXML
    private void initialize() {
        webView.getEngine().getLoadWorker().stateProperty().addListener(
                (observable, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        PauseTransition pause =
                                new PauseTransition(Duration.millis(250));

                        pause.setOnFinished(event -> {
                            showLocationsOnMap();
                            drawRouteOnMap();
                        });

                        pause.play();
                    }
                }
        );
    }

    /**
     * Shows a single address on the map
     * @param address The address to show
     */
    public void showAddressOnMap(Address address) {
        showAddressesOnMap(new Address[]{address});
    }

    /**
     * Shows an array of addresses on points on a map
     * @param addresses The array of addresses to show
     */
    public void showAddressesOnMap(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            throw new IllegalArgumentException(
                    "You must provide at least one address."
            );
        }

        Task<MapLocation[]> task = new Task<>() {
            @Override
            protected MapLocation[] call() throws Exception {
                MapLocation[] locations = new MapLocation[addresses.length];

                for (int i = 0; i < addresses.length; i++) {
                    Coordinates coordinates = MapTools.getInstance()
                            .getCoordinates(addresses[i]);

                    locations[i] = new MapLocation(
                            addresses[i],
                            coordinates
                    );
                }

                return locations;
            }
        };

        task.setOnSucceeded(event -> {
            locationsToShow = task.getValue();
            loadMapPage();
        });

        task.setOnFailed(event -> {
            task.getException().printStackTrace();
        });

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void loadMapPage() {
        String mapPage = MapController.class
                .getResource("map.html")
                .toExternalForm();

        webView.getEngine().load(mapPage);
    }

    private void showLocationsOnMap() {
        if (locationsToShow == null) {
            return;
        }

        StringBuilder javascript = new StringBuilder(
                "window.showLocations(["
        );

        for (int i = 0; i < locationsToShow.length; i++) {
            MapLocation location = locationsToShow[i];

            double latitude = location.getCoordinates().getLatitude();
            double longitude = location.getCoordinates().getLongitude();
            String address = location.getAddress().toString();

            javascript.append("{")
                    .append("latitude: ")
                    .append(latitude)
                    .append(", longitude: ")
                    .append(longitude)
                    .append(", address: ")
                    .append(makeJavaScriptString(address))
                    .append("}");

            if (i < locationsToShow.length - 1) {
                javascript.append(",");
            }
        }

        javascript.append("]);");

        webView.getEngine().executeScript(javascript.toString());
    }

    private String makeJavaScriptString(String text) {
        return "'" + text
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                + "'";
    }

    /**
     * Shows a route line on map from one address to another
     * @param startingAddress The address the route starts from
     * @param endingAddress The address the route ends on
     */
    public void showRouteOnMap(Address startingAddress, Address endingAddress) {
        Task<DrivingRoute> task = new Task<>() {
            @Override
            protected DrivingRoute call() throws Exception {
                return MapTools.getInstance()
                        .getDrivingRoute(startingAddress, endingAddress);
            }
        };

        task.setOnSucceeded(event -> {
            DrivingRoute route = task.getValue();

            locationsToShow = new MapLocation[]{
                    route.getStartingLocation(),
                    route.getEndingLocation()
            };

            routePointsToShow = route.getRoutePoints();

            loadMapPage();
        });

        task.setOnFailed(event -> {
            task.getException().printStackTrace();
        });

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void drawRouteOnMap() {
        if (routePointsToShow == null) {
            return;
        }

        StringBuilder javascript = new StringBuilder(
                "window.showRoute(["
        );

        for (int i = 0; i < routePointsToShow.size(); i++) {
            Coordinates point = routePointsToShow.get(i);

            javascript.append("[")
                    .append(point.getLatitude())
                    .append(",")
                    .append(point.getLongitude())
                    .append("]");

            if (i < routePointsToShow.size() - 1) {
                javascript.append(",");
            }
        }

        javascript.append("]);");

        webView.getEngine().executeScript(javascript.toString());
    }
}