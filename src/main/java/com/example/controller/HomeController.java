package com.example.controller;

import static org.apache.spark.sql.functions.*; // Importation des fonctions de Spark SQL
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public class HomeController {

    @FXML
    private WebView webView;
    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField searchField;

    private SparkSession spark;
    private Dataset<Row> data;

    @FXML
    public void initialize() {
        // Initialisation de Spark
        try {
            spark = SparkSession.builder()
                    .appName("SearchLocationApp")
                    .master("local")
                    .getOrCreate();

            // Chargement des données depuis le fichier CSV
            data = spark.read()
                    .option("header", "true")
                    .csv(getClass().getResource("/zone.csv").toExternalForm()); // Utilisation de getResource pour le chemin

            WebEngine webEngine = webView.getEngine();
            // Chargez le fichier HTML depuis le dossier html dans resources
            webEngine.load(getClass().getResource("/html/index.html").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de Spark ou du chargement des données : " + e.getMessage());
        }
    }

    public void setUserDetails(String username, int userId) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + username + " !");
        } else {
            System.err.println("Le label 'welcomeLabel' est null !");
        }
    }

    // Méthode pour gérer la recherche
    @FXML
    private void handleSearch() {
        String searchQuery = searchField.getText().trim(); // Élimine les espaces autour
        if (!searchQuery.isEmpty()) {
            searchLocation(searchQuery);
        } else {
            System.out.println("Veuillez entrer une requête de recherche.");
        }
    }

    private void searchLocation(String query) {
        try {
            // Filtrer les résultats où le nom contient la requête de recherche, insensible à la casse
            Dataset<Row> results = data.filter(lower(data.col("name")).contains(query.toLowerCase())); // Utilisation de lower()

            // Si des résultats sont trouvés, afficher sur la carte
            if (results.count() > 0) {
                List<Row> rows = results.collectAsList();
                StringBuilder jsonArray = new StringBuilder("[");

                for (Row row : rows) {
                    String name = row.getAs("name");
                    String latitude = row.getAs("latitude").toString();
                    String longitude = row.getAs("longitude").toString();

                    jsonArray.append(String.format("{\"name\":\"%s\", \"lat\":%s, \"lng\":%s},", name, latitude, longitude));
                }

                // Retirer la dernière virgule si nécessaire
                if (jsonArray.length() > 1) {
                    jsonArray.setLength(jsonArray.length() - 1);
                }
                jsonArray.append("]");

                // Exécuter le script JavaScript pour mettre à jour la carte
                webView.getEngine().executeScript("updateMap(" + jsonArray.toString() + ");");
            } else {
                System.out.println("Aucun lieu trouvé pour la recherche : " + query);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche de la localisation : " + e.getMessage());
        }
    }

}
