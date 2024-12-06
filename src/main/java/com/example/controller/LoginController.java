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
import javafx.scene.layout.Pane;
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

    // Instance de la classe UserService
    private UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez entrer un nom d'utilisateur et un mot de passe.");
            return;
        }

        loadingIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (userService.authenticate(username, password)) {
                    return null;
                } else {
                    throw new Exception("Nom d'utilisateur ou mot de passe incorrect !");
                }
            }

            @Override
            protected void succeeded() {
                loadingIndicator.setVisible(false);
                errorLabel.setText("Connexion réussie !");
                int userId = userService.getUserIdByUsername(username);
                redirectToHome(username, userId);
            }

            @Override
            protected void failed() {
                loadingIndicator.setVisible(false);
                errorLabel.setText(getMessage());
            }
        };

        new Thread(task).start();
    }

    private void redirectToHome(String username, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            AnchorPane homeRoot = loader.load();

            Scene scene = new Scene(homeRoot);
            scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

            HomeController homeController = loader.getController();
            if (homeController != null) {
                homeController.setUserDetails(username, userId);
            }

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
            Pane signupRoot = loader.load(); // Utilisation de Pane pour éviter les incompatibilités

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
