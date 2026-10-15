module com.movement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.movement to javafx.fxml;
    exports com.movement;
}
