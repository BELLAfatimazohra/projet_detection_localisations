package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {
    @FXML
    private Label welcomeLabel;

    public void setUserDetails(String username, int userId) {
        // Assurez-vous que welcomeLabel n'est pas null avant de l'utiliser
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + username + " !");
        }
    }
}
