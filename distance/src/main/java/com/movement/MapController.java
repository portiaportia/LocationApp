package com.movement;

import com.location.Address;
import com.location.Coordinates;
import com.location.MapTools;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class MapController {

    @FXML
    private WebView webView;

    private Coordinates locationToShow;
    private String addressToShow;

    @FXML
    private void initialize() {
        /*
         * Wait until map.html has loaded before asking its JavaScript
         * to show the location.
         */
        webView.getEngine().getLoadWorker().stateProperty().addListener(
                (observable, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        showLocationOnMap();
                    }
                }
        );
    }

    /**
     * Finds an address's coordinates, then displays it on the map.
     */
    public void showAddressOnMap(Address address) {
        Task<Coordinates> task = new Task<>() {
            @Override
            protected Coordinates call() throws Exception {
                return MapTools.getInstance().getCoordinates(address);
            }
        };

        task.setOnSucceeded(event -> {
            locationToShow = task.getValue();
            addressToShow = address.toString();

            loadMapPage();
        });

        task.setOnFailed(event -> {
            task.getException().printStackTrace();
        });

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    /*
     * Loads the separate HTML page into the WebView.
     */
    private void loadMapPage() {
        String mapPage = MapController.class
                .getResource("map.html")
                .toExternalForm();

        webView.getEngine().load(mapPage);
    }

    /*
     * Calls the showLocation function inside map.js.
     */
    private void showLocationOnMap() {
        if (locationToShow == null) {
            return;
        }

        String safeAddress = addressToShow
                .replace("\\", "\\\\")
                .replace("'", "\\'");

        String javascript = "window.showLocation("
                + locationToShow.getLatitude() + ", "
                + locationToShow.getLongitude() + ", '"
                + safeAddress + "')";

        webView.getEngine().executeScript(javascript);
    }
}