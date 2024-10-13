
module com.example {
    requires javafx.controls; // Nécessaire pour JavaFX
    requires javafx.fxml; // Nécessaire pour JavaFX FXML
    requires com.dlsc.formsfx; // Nécessaire si vous utilisez FormsFX
    requires java.sql; // Nécessaire pour JDBC

    opens com.example to javafx.fxml; // Ouvre le package com.example pour JavaFX
    exports com.example; // Exporte le package com.example
}
