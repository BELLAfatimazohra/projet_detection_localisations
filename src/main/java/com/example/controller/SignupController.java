package com.example.controller;

import com.example.service.UserService;
import com.example.model.User;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.regex.Pattern;

public class SignupController {
    public Label errorLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button signupButton;

    private final UserService userService = new UserService();

    // Regex for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    // Method called on "Sign Up" button click
    @FXML
    public void handleSignup(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Validate the fields
            if (!validateFields(username, email, password, confirmPassword)) {
                return;
            }

            // Create new user
            User user = new User(username, email, password);

            // Register the user using the service
            boolean isRegistered = userService.register(user);

            if (isRegistered) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful!");
                loadLoginPage(event); // Load login page
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Registration failed.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    // Validate the fields before registration
    private boolean validateFields(String username, String email, String password, String confirmPassword) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "All fields must be filled.");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Invalid email format.");
            return false;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Password must be at least 6 characters long.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Passwords do not match.");
            return false;
        }

        return true;
    }

    // Display an alert
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Load login page after successful registration
    private void loadLoginPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent loginRoot = loader.load();

        Scene loginScene = new Scene(loginRoot);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(loginScene);
        stage.show();
    }

    // Redirect to login page when user clicks the "Already have an account?" link
    @FXML
    public void handleLoginRedirect(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent loginRoot = loader.load();

        Scene loginScene = new Scene(loginRoot);
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(loginScene);
        stage.show();
    }
}