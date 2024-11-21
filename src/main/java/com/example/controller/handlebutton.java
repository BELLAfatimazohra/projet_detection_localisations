package com.example.controller;

import javafx.fxml.FXML;

public class handlebutton {

    @FXML
    private void handleprofile() {
        System.out.println("Le bouton Profil a été cliqué !");
        // Logique spécifique pour le bouton Profil
    }

    @FXML
    private void handleadresseenredgistre() {
        System.out.println("Le bouton Adresse Enregistrée a été cliqué !");
        // Logique spécifique pour le bouton Adresse Enregistrée
    }

    @FXML
    private void handleLogout() {
        System.out.println("Le bouton Logout a été cliqué !");
        // Logique spécifique pour la déconnexion
    }

    @FXML
    private void handlerecherherecente() {
        System.out.println("Le bouton Recherche Récente a été cliqué !");
        // Logique spécifique pour la recherche récente
    }
}
