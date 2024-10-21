package com.example.controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HomeController {

    @FXML
    private WebView webView;
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        WebEngine webEngine = webView.getEngine();
        // Chargez le fichier HTML depuis le dossier html dans resources
        webEngine.load(getClass().getResource("/html/index.html").toExternalForm());
    }
    public void setUserDetails(String username, int userId) {
        // Afficher un message de bienvenue dans le label
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + username + " !");
        } else {
            System.err.println("Le label 'welcomeLabel' est null !");
        }
    }
}
