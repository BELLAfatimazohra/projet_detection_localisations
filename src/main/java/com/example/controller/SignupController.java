package com.example.controller;

import com.example.service.UserService;
import com.example.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.regex.Pattern;

public class SignupController {

    public TextField usernameField;
    public TextField emailField;
    public TextField passwordField;
    public Button signupButton;

    private final UserService userService = new UserService();

    // Regex pour valider l'email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    // Méthode appelée lors du clic sur "S'inscrire"
    public void handleSignup(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            // Valider les champs
            if (!validateFields(username, email, password)) {
                return;
            }

            // Créer un nouvel utilisateur
            User user;
            user = new User(username, email, password);

            // Enregistrer l'utilisateur via le service
            boolean isRegistered = userService.register(user);

            if (isRegistered) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie !");
                loadLoginPage(event); // Charger la page de login
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Inscription échouée.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    // Valider les champs avant l'inscription
    private boolean validateFields(String username, String email, String password) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Tous les champs doivent être remplis.");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "L'email est invalide.");
            return false;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }

        return true;
    }

    // Afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Charger la page de login
    private void loadLoginPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent loginRoot = loader.load();

        Scene loginScene = new Scene(loginRoot);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(loginScene);
        stage.show();
    }
}
