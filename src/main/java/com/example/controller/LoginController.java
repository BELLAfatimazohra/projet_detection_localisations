package com.example.controller;

import com.example.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Vérifie si l'utilisateur est authentifié
        if (userService.authenticate(username, password)) {
            errorLabel.setText("Connexion réussie !");

            // Récupère l'ID de l'utilisateur connecté
            int userId = userService.getUserIdByUsername(username);

            // Redirige vers la page d'accueil en passant l'ID de l'utilisateur
            redirectToHome(username, userId);
        } else {
            errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect !");
        }
    }

    private void redirectToHome(String username, int userId) {
        try {
            // Charger la page "home.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            VBox homeRoot = loader.load();

            // Obtenir le contrôleur HomeController et lui passer l'ID et le nom de l'utilisateur
            HomeController homeController = loader.getController();
            if (homeController != null) {
                homeController.setUserDetails(username, userId);
            }

            // Obtenir la fenêtre actuelle et remplacer la scène
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(homeRoot);
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
}
