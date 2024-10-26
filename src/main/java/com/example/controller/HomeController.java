package com.example.controller;

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
import org.json.JSONObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

public class HomeController {

    @FXML
    private WebView webView;
    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField searchField;

    // Labels pour la barre latérale
    @FXML
    private Label locationNameLabel; // Label pour le nom de l'emplacement
    @FXML
    private Label locationDescriptionLabel; // Label pour la description de l'emplacement

    private SparkSession spark;

    @FXML
    public void initialize() {
        // Initialisation de la session Spark
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

    private void searchLocation(String query) {
        String url;
        try {
            // Encoder le query pour l'utiliser dans l'URL
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&addressdetails=1&limit=10";

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(request);

                // Vérifier si la réponse est réussie (code 200)
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

                        // Retirer la dernière virgule si nécessaire
                        if (jsonArray.length() > 1) {
                            jsonArray.setLength(jsonArray.length() - 1);
                        }
                        jsonArray.append("]");

                        // Mettre à jour la carte avec le résultat
                        webView.getEngine().executeScript("updateMap(" + jsonArray.toString() + ");");

                        // Convertir le résultat en DataFrame Spark
                        Dataset<Row> locationsDF = spark.read().json(spark.createDataset(Collections.singletonList(jsonArray.toString()), org.apache.spark.sql.Encoders.STRING()));
                        locationsDF.show();  // Afficher le DataFrame pour vérifier les résultats

                        // Obtenir les détails de Wikipedia pour le premier emplacement trouvé
                        String firstLocationName = results.getJSONObject(0).optString("display_name");
                        getWikipediaDetails(firstLocationName);

                    } else {
                        System.out.println("Aucun lieu trouvé pour la recherche : " + query);
                    }
                } else {
                    System.err.println("Erreur lors de la requête HTTP : " + response.getStatusLine());
                }
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("Erreur d'encodage de l'URL : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur lors de la recherche de la localisation : " + e.getMessage());
        }
    }

    public void getWikipediaDetails(String locationName) throws UnsupportedEncodingException {
        String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" +
                URLEncoder.encode(locationName, "UTF-8") + "&format=json&origin=*";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(request);

            // Vérifier si la réponse est réussie (code 200)
            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray searchResults = jsonObject.getJSONObject("query").getJSONArray("search");

                if (searchResults.length() > 0) {
                    JSONObject firstResult = searchResults.getJSONObject(0);
                    String title = firstResult.getString("title");
                    String snippet = firstResult.getString("snippet");

                    // Mettre à jour la barre latérale avec les détails
                    locationNameLabel.setText("Nom : " + title);
                    locationDescriptionLabel.setText("Description : " + snippet);
                } else {
                    System.out.println("Aucun résultat trouvé sur Wikipedia pour : " + locationName);
                }
            } else {
                System.err.println("Erreur lors de la requête à Wikipedia : " + response.getStatusLine());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'obtention des détails de Wikipedia : " + e.getMessage());
        }
    }
}
