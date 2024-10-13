module com.example.projet_detection_localisations {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.projet_detection_localisations to javafx.fxml;
    exports com.example.projet_detection_localisations;
}