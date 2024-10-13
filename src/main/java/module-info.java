module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.example;
    exports com.example.controller;
    opens com.example.controller to javafx.fxml;
    opens com.example to javafx.fxml;
}
