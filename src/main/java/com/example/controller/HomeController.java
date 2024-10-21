package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HomeController {

    @FXML
    private WebView mapWebView;

    @FXML
    private Label welcomeLabel;  // Label pour le message de bienvenue

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur HomeController");

        // Initialiser la carte dans WebView
        WebEngine webEngine = mapWebView.getEngine();
        String mapHtml = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='utf-8' />"
                + "<title>Carte du Maroc</title>"
                + "<style>html, body, #map { height: 100%; margin: 0; }</style>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />"
                + "<script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>"
                + "</head>"
                + "<body>"
                + "<div id='map'></div>"
                + "<script>"
                + "var map = L.map('map').setView([31.7917, -7.0926], 6);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {"
                + "  maxZoom: 18,"
                + "  attribution: '© OpenStreetMap contributors'"
                + "}).addTo(map);"
                + "L.marker([31.7917, -7.0926]).addTo(map)"
                + "  .bindPopup('Vous êtes ici.').openPopup();"
                + "</script>"
                + "</body>"
                + "</html>";

        webEngine.loadContent(mapHtml);
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
