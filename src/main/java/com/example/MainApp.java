package com.example;

import com.example.service.UserService; // Importation de UserService
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        loadLoginInterface(primaryStage); // Charge l'interface de connexion au démarrage
    }

    private void loadLoginInterface(Stage primaryStage) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
            System.out.println("Chemin recherché: " + fxmlUrl); // Affichez le chemin pour le débogage
            if (fxmlUrl == null) {
                System.out.println("Le fichier FXML est introuvable !");
                return; // Arrêter si le fichier FXML est introuvable
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Titre de votre application");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UserService userService = new UserService();
        userService.printDatabaseName(); // Affiche le nom de la base de données
        launch(args);
    }
}
