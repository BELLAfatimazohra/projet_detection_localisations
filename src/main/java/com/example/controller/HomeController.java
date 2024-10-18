package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import com.example.service.LocationService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HomeController {

    @FXML
    private VBox mapContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    private WebView webView; // Déclaration du WebView
    private LocationService locationService; // Service pour charger les données OSM

    // Détails de l'utilisateur
    private String username;
    private int userId;

    public void initialize() {
        // Initialisation de la carte
        initializeMap();

        // Initialiser le service de localisation avec le fichier OSM
        locationService = new LocationService();
        loadOSMData();
    }

    private void loadOSMData() {
        try {
            // Chargement du fichier OSM depuis les ressources
            InputStream osmData = getClass().getClassLoader().getResourceAsStream("data/morocco-latest.osm.pbf");
            if (osmData == null) {
                throw new FileNotFoundException("Le fichier OSM n'a pas été trouvé.");
            }

            // Sauvegarde temporaire sur disque
            File tempFile = File.createTempFile("temp_osm", ".pbf");
            tempFile.deleteOnExit(); // Supprime le fichier temporaire à la fermeture de l'application

            // Écriture du contenu du fichier OSM dans un fichier temporaire
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = osmData.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
            }

            // Passer le chemin du fichier à la méthode de LocationService
            locationService.loadOSMData(tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // Initialiser la carte dans le conteneur
    private void initializeMap() {
        webView = new WebView(); // Crée un nouveau WebView
        webView.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE); // Ajuste à la taille disponible
        WebEngine webEngine = webView.getEngine();

        // Charge la carte OpenStreetMap centrée sur le Maroc (coordonnées ajustées)
        String url = "https://www.openstreetmap.org/export/embed.html?bbox=-17.0%2C20.0%2C-0.5%2C37.5&layer=mapnik&marker=33.0%2C-7.5";
        webEngine.load(url);

        // Ajoute le WebView au conteneur
        mapContainer.getChildren().add(webView);

        // Ajuster la taille du conteneur pour qu'il prenne tout l'écran
        VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS); // Permet au WebView de grandir pour remplir l'espace
    }

    // Méthode appelée lorsque le bouton "Rechercher" est cliqué
    @FXML
    public void onSearchButtonClick() {
        String locationName = searchField.getText();
        if (locationName != null && !locationName.isEmpty()) {
            searchLocation(locationName);
        }
    }

    // Recherche une localisation via le service de localisation
    private void searchLocation(String locationName) {
        Dataset<Row> result = locationService.searchLocation(locationName);

        // Gérer les résultats et mettre à jour la carte
        if (result != null && result.count() > 0) {
            // Par exemple, vous pouvez obtenir les coordonnées et centrer la carte
            double latitude = result.first().getDouble(0); // Ajustez l'index selon la structure de vos données
            double longitude = result.first().getDouble(1); // Ajustez l'index selon la structure de vos données

            // Mettre à jour l'URL de la carte pour centrer sur la nouvelle localisation
            String url = String.format("https://www.openstreetmap.org/export/embed.html?bbox=%f%%2C%f%%2C%f%%2C%f&layer=mapnik&marker=%f%%2C%f",
                    longitude - 0.01, latitude - 0.01, longitude + 0.01, latitude + 0.01, longitude, latitude);
            webView.getEngine().load(url);
        } else {
            // Gérer le cas où aucune localisation n'est trouvée
            System.out.println("Aucune localisation trouvée pour : " + locationName);
        }
    }

    // Méthode pour définir les détails de l'utilisateur
    public void setUserDetails(String username, int userId) {
        this.username = username;
        this.userId = userId;
        // Logique additionnelle si nécessaire
    }
}
