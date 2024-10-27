package com.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class HomeController {

    @FXML
    private WebView webView;
    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Label locationNameLabel;
    @FXML
    private Label locationDescriptionLabel;

    private SparkSession spark;

    @FXML
    public void initialize() {
        spark = SparkSession.builder()
                .appName("SearchLocationApp")
                .master("local")
                .getOrCreate();

        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/html/index.html").toExternalForm());
    }

    public void setUserDetails(String username, int userId) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + username + " !");
        } else {
            System.err.println("Le label 'welcomeLabel' est null !");
        }
    }

    @FXML
    private void handleSearch() {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            searchLocation(searchQuery);
        } else {
            System.out.println("Veuillez entrer une requête de recherche.");
        }
    }
//
    private void searchLocation(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&addressdetails=1&limit=10";

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(request)) {

                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JSONArray results = new JSONArray(jsonResponse);

                        if (results.length() > 0) {
                            StringBuilder jsonArray = new StringBuilder("[");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject location = results.getJSONObject(i);
                                String name = location.optString("display_name");
                                String latitude = location.optString("lat");
                                String longitude = location.optString("lon");

                                jsonArray.append(String.format("{\"name\":\"%s\", \"lat\":%s, \"lng\":%s},", name, latitude, longitude));
                            }

                            if (jsonArray.length() > 1) {
                                jsonArray.setLength(jsonArray.length() - 1);
                            }
                            jsonArray.append("]");

                            webView.getEngine().executeScript("updateMap(" + jsonArray.toString() + ");");

                            Dataset<Row> locationsDF = spark.read().json(spark.createDataset(Collections.singletonList(jsonArray.toString()), org.apache.spark.sql.Encoders.STRING()));
                            locationsDF.show();

                            // Utiliser le nom du premier emplacement trouvé pour charger la page Wikipedia
                            String firstLocationName = results.getJSONObject(0).optString("display_name");
                            getWikipediaDetails(firstLocationName);
                        } else {
                            System.out.println("Aucun lieu trouvé pour la recherche : " + query);
                        }
                    } else {
                        System.err.println("Erreur lors de la requête HTTP : " + response.getStatusLine());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la recherche de la localisation : " + e.getMessage());
        }
    }

    private void getWikipediaDetails(String locationName) {
        try {
            // URL de l'API de Wikipedia pour récupérer un extrait de l'article
            String apiUrl = "https://fr.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(locationName, "UTF-8");

            // Créer une requête HTTP pour l'API
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .build();

            // Envoyer la requête et traiter la réponse
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        try {
                            JSONObject json = new JSONObject(response);

                            // Récupérer uniquement l'extrait du texte
                            String extract = json.optString("extract", "Aucune information disponible.");

                            // Afficher uniquement l'extrait dans le label
                            Platform.runLater(() -> locationDescriptionLabel.setText(extract));

                        } catch (JSONException e) {
                            Platform.runLater(() -> locationDescriptionLabel.setText("Erreur lors du chargement des informations."));
                        }
                    })
                    .exceptionally(e -> {
                        Platform.runLater(() -> locationDescriptionLabel.setText("Erreur de connexion à l'API Wikipedia."));
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la page Wikipedia : " + e.getMessage());
            Platform.runLater(() -> locationDescriptionLabel.setText("Informations historiques non disponibles."));
        }
    }
// hi kanjrb lpush
}
