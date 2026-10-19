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

        Scene scene = new Scene(root, 900, 650);

        stage.setScene(scene);
        stage.show();

        controller.showAddressOnMap(userAddress);
    }

    public static void main(String[] args) {
        launch();
    }
}