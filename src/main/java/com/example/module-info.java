module com.example {
    requires org.apache.spark.core;
    requires org.apache.spark.sql;
    requires java.sql;
    requires org.slf4j; // Si vous utilisez SLF4J pour le logging
    requires javafx.controls; // Pour JavaFX
    requires javafx.fxml;

    exports com.example;
}
