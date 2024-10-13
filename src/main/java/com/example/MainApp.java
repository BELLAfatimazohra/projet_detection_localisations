package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Exemple d'interface simple
        Label label = new Label("Bienvenue dans JavaFX avec Oracle DB !");
        StackPane root = new StackPane();
        root.getChildren().add(label);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("JavaFX + Oracle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);

        // Appeler la méthode de test de connexion ici après avoir lancé l'interface
        OracleDBTest.testConnection();
    }
}
