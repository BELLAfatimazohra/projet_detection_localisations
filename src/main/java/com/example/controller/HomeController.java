package com.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeController {

    @FXML
    private ListView<String> resultsListView;
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

    @FXML
    private ListView<String> suggestionsList; // Nouvelle liste pour les suggestions

    private SparkSession spark;

    @FXML
    public void initialize() {
        // Initialisation de SparkSession
        spark = SparkSession.builder()
                .appName("SearchLocationApp")
                .master("local")
                .getOrCreate();

        // Chargement de la page HTML dans WebView
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/html/index.html").toExternalForm());

        // Vérification si suggestionsList est initialisé
        if (suggestionsList == null) {
            System.out.println("suggestionsList is null");
        } else {
            suggestionsList.setVisible(false); // Masquer la liste des suggestions au départ
        }
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

    @FXML
    private void onSearchFieldKeyTyped(KeyEvent event) {
        String query = searchField.getText();
        if (query.length() > 1) { // Démarrer la recherche après 2 caractères
            fetchSuggestions(query);
        } else {
            suggestionsList.setVisible(false); // Masquer si la longueur est <= 1
        }
    }

    private void fetchSuggestions(String query) {
        String urlString = "https://nominatim.openstreetmap.org/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&format=json&addressdetails=1&limit=10";
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                List<String> suggestions = new ArrayList<>();

                // Analysez la réponse JSON et extrayez les noms de lieu
                StringBuilder jsonResponse = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    jsonResponse.append(inputLine);
                }
                in.close();

                JSONArray results = new JSONArray(jsonResponse.toString());
                for (int i = 0; i < results.length(); i++) {
                    JSONObject location = results.getJSONObject(i);
                    String name = location.optString("display_name");
                    suggestions.add(name); // Ajoutez le nom à la liste des suggestions
                }

                // Mettez à jour la liste des suggestions sur le JavaFX Application Thread
                updateSuggestions(suggestions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateSuggestions(List<String> suggestions) {
        // Mettez à jour la liste des suggestions et affichez-la
        suggestionsList.getItems().clear();
        suggestionsList.getItems().addAll(suggestions);
        suggestionsList.setVisible(!suggestions.isEmpty());
    }

    @FXML
    private void onSuggestionClicked() {
        String selectedPlace = suggestionsList.getSelectionModel().getSelectedItem();
        if (selectedPlace != null) {
            searchField.setText(selectedPlace); // Remplir le champ de recherche avec la suggestion choisie
            suggestionsList.setVisible(false); // Masquer la liste après la sélection
            searchLocation(selectedPlace); // Rechercher la localisation sélectionnée
        }
    }

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
                                jsonArray.setLength(jsonArray.length() - 1); // Remove the last comma
                            }
                            jsonArray.append("]");

                            // Mise à jour de la carte
                            webView.getEngine().executeScript("updateMap(" + jsonArray.toString() + ");");

                            // Création d'un DataFrame Spark
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

    @FXML
    private void handleSearchWikipedia() {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            getWikipediaDetails(searchQuery);
        } else {
            System.out.println("Veuillez entrer un nom de localisation.");
        }
    }

    private void getWikipediaDetails(String locationName) {
        // Prendre uniquement le nom principal avant la première virgule ou espace
        String[] parts = locationName.split(",|\\s+");
        final String finalLocationName = parts[0].trim(); // Déclarez comme final

        // Afficher le nom de la localisation pour débogage
        System.out.println("Nom de la localisation pour Wikipedia : " + finalLocationName);

        // Construire l'URL de l'API
        String apiUrl = "https://fr.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(finalLocationName, StandardCharsets.UTF_8);

        // Afficher l'URL de l'API pour vérifier
        System.out.println("URL de l'API: " + apiUrl);

        // Requête HTTP à l'API
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        String extract = json.getString("extract");
                        String displayTitle = json.getString("title");

                        // Mettre à jour l'interface utilisateur avec les informations
                        Platform.runLater(() -> {
                            locationNameLabel.setText(displayTitle);
                            locationDescriptionLabel.setText(extract);
                        });
                    } catch (JSONException e) {
                        System.err.println("Erreur lors de la parsing de la réponse de Wikipedia : " + e.getMessage());
                    }
                });
    }
    @FXML
    private void handleItinerary() {
        // Logique pour afficher ou calculer l'itinéraire
        System.out.println("Itinéraire demandé pour : " + locationNameLabel.getText());

        // Vous pouvez ajouter ici du code pour afficher l'itinéraire dans WebView ou un autre élément
    }
}
