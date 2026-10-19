package com.movement;

import com.location.Address;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("map_view.fxml")
        );

        Parent root = loader.load();
        MapController controller = loader.getController();

        Address userAddress = new Address(
                "1 Vendue Range",
                "Charleston",
                "SC",
                "29401"
        );

        Address[] addresses = {
                new Address(
                        "1 Vendue Range",
                        "Charleston",
                        "SC",
                        "29401"
                ),

                new Address(
                        "2950 Carner Avenue",
                        "North Charleston",
                        "SC",
                        "29405"
                ),

                new Address(
                        "3841 Leeds Avenue",
                        "North Charleston",
                        "SC",
                        "29405"
                ),

                new Address(
                        "3625 Ashley Phosphate Road",
                        "North Charleston",
                        "SC",
                        "29418"
                )
        };

        Scene scene = new Scene(root, 900, 650);

        stage.setScene(scene);
        stage.show();

        //controller.showAddressOnMap(userAddress);
        //controller.showAddressesOnMap(addresses);

        controller.showRouteOnMap(userAddress,addresses[1]);
    }

    public static void main(String[] args) {
        launch();
    }
}