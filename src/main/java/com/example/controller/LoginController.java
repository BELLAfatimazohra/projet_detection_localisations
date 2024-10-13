package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import com.example.service.UserService;

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
        if (userService.authenticate(username, password)) {
            errorLabel.setText("Connexion réussie !");
        } else {
            errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect !");
        }
    }
}
