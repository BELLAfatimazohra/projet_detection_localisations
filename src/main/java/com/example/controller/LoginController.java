package com.example.controller;

import com.example.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.io.IOException;

public class LoginController {
    public ImageView backgroundImage;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button signupButton;
    @FXML
    private Button loginButton; // Ajouté pour référencer le bouton de connexion
    @FXML
    private ProgressIndicator loadingIndicator; // Indicateur de chargement
// creation d'une instance de la classe UserService
    private UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validation des champs
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez entrer un nom d'utilisateur et un mot de passe.");
            return;
        }

        // Afficher l'indicateur de chargement
        loadingIndicator.setVisible(true);

        // Exécuter la tâche de connexion dans un nouveau thread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Vérifie si l'utilisateur est authentifié
                if (userService.authenticate(username, password)) {
                    return null; // Authentification réussie
                } else {
                    throw new Exception("Nom d'utilisateur ou mot de passe incorrect !");
                }
            }

            @Override
            protected void succeeded() {
                loadingIndicator.setVisible(false); // Masquer l'indicateur de chargement
                errorLabel.setText("Connexion réussie !");

                // Récupère l'ID de l'utilisateur connecté
                int userId = userService.getUserIdByUsername(username);

                // Redirige vers la page d'accueil en passant l'ID de l'utilisateur
                redirectToHome(username, userId);
            }

            @Override
            protected void failed() {
                loadingIndicator.setVisible(false); // Masquer l'indicateur de chargement
                errorLabel.setText(getMessage()); // Afficher le message d'erreur
            }
        };

        // Démarrer la tâche dans un nouveau thread
        new Thread(task).start();
    }

    private void redirectToHome(String username, int userId) {
        try {
            System.out.println("Tentative de chargement de home.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            AnchorPane homeRoot = loader.load();  // Changer VBox à AnchorPane
            System.out.println("Chargement réussi de home.fxml");

            // Appliquer le fichier CSS ici
            Scene scene = new Scene(homeRoot);
            scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
            System.out.println("Fichier CSS chargé avec succès.");

            // Obtenir le contrôleur HomeController et lui passer l'ID et le nom de l'utilisateur
            HomeController homeController = loader.getController();
            if (homeController != null) {
                homeController.setUserDetails(username, userId);
            }

            // Obtenir la fenêtre actuelle et remplacer la scène
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger la page d'accueil.");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
            VBox signupRoot = loader.load();

            Stage stage = (Stage) signupButton.getScene().getWindow();
            Scene scene = new Scene(signupRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger la page d'inscription.");
        }
    }
}
