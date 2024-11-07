
package com.example.controller;
import com.example.service.UserService;
import com.example.model.User;
import com.example.database.DatabaseConnection;
import com.example.service.GeolocationService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HomeController {

    @FXML
    private Button itineraryButton;
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
    private ListView<String> suggestionsList;

    private SparkSession spark;
    private GeolocationService geolocationService;

    public HomeController() {
        this.geolocationService = new GeolocationService();
    }

    @FXML
    public void handleGetLocation() {
        double[] coordinates = geolocationService.getLocationCoordinates();
        if (coordinates != null && coordinates.length == 2) {
            String script = String.format(Locale.US, "addMarker(%f, %f);", coordinates[1], coordinates[0]);
            System.out.println("Latitude : " + coordinates[0] + ", Longitude: " + coordinates[1]);
            webView.getEngine().executeScript(script);
            webView.getEngine().executeScript("alert('Test JavaScript depuis Java');");
        } else {
            System.err.println("Les coordonnées sont nulles ou incomplètes.");
        }
    }



    @FXML
    public void initialize() {
        // Initialiser le service de géolocalisation
        spark = SparkSession.builder()
                .appName("SearchLocationApp")
                .master("local")
                .getOrCreate();

        // Charger le WebView avec le fichier HTML
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/html/index.html").toExternalForm());

        // Ajoutez un écouteur pour vérifier quand la carte est prête
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("Carte prête pour les interactions.");
                webEngine.executeScript("alert('Carte chargée');"); // Test pour confirmer le chargement
            }
        });
        webEngine.setOnError(event -> System.out.println("Erreur JavaScript : " + event.getMessage()));

        webEngine.setOnAlert(event -> System.out.println(event.getData())); // pour capturer les alertes JavaScript
        System.out.println("Carte chargée : " + webEngine.getLoadWorker().getState());




        // S'assurer que suggestionsList est correctement lié
        if (suggestionsList != null) {
            suggestionsList.setVisible(false);
        } else {
            System.err.println("Erreur: suggestionsList est null.");
        }
        System.out.println("Carte chargée.");

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

                            String firstLocationName = results.getJSONObject(0).optString("display_name");
                            getWikipediaDetails(firstLocationName);

                            storeSearchInDatabase(query);
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

    public int getUserIdByUsername(String username) {
        String query = "SELECT id FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                System.out.println("Utilisateur non trouvé.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Retourne -1 en cas d'échec
    }

    private void storeSearchInDatabase(String searchQuery) {
        // Récupérer le nom d'utilisateur à partir du welcomeLabel
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");

        // Vérifiez si le username est valide
        if (username.isEmpty()) {
            System.err.println("Nom d'utilisateur introuvable !");
            return;
        }

        // Afficher le nom d'utilisateur pour le test
        System.out.println("Nom d'utilisateur recherché : " + username);

        // Récupérer l'ID de l'utilisateur en utilisant la fonction existante
        int userId = getUserIdByUsername(username);

        // Afficher l'ID de l'utilisateur connecté
        if (userId != -1) {
            System.out.println("ID de l'utilisateur connecté : " + userId);

            // Vérifier si l'utilisateur existe avant l'insertion
            String insertQuery = "INSERT INTO SEARCH_HISTORY (name, user_id) VALUES (?, ?)";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, searchQuery);
                preparedStatement.setInt(2, userId);
                preparedStatement.executeUpdate();
                System.out.println("Recherche enregistrée : " + searchQuery + " pour l'utilisateur ID : " + userId);
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'enregistrement de la recherche : " + e.getMessage());
            }
        } else {
            System.err.println("L'utilisateur n'existe pas dans la base de données.");
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
        String[] parts = locationName.split(",|\\s+");
        final String finalLocationName = parts[0].trim();

        String apiUrl = "https://fr.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(finalLocationName, StandardCharsets.UTF_8);
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

                        Platform.runLater(() -> {
                            locationNameLabel.setText(displayTitle);
                            locationDescriptionLabel.setText(extract);
                        });
                    } catch (JSONException e) {
                        System.err.println("Erreur lors de la récupération des détails Wikipedia : " + e.getMessage());
                    }
                });
    }

    @FXML
    private void onSearchFieldClicked() {
        // Récupérer l'historique de recherche de l'utilisateur et l'afficher
        fetchUserSearchHistory();
    }

    private void fetchUserSearchHistory() {
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");
        int userId = getUserIdByUsername(username);

        if (userId != -1) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT name FROM SEARCH_HISTORY WHERE user_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, userId);
                    ResultSet resultSet = statement.executeQuery();
                    List<String> history = new ArrayList<>();
                    while (resultSet.next()) {
                        history.add(resultSet.getString("name"));
                    }

                    // Affichage de l'historique de recherche
                    Platform.runLater(() -> {
                        suggestionsList.getItems().setAll(history);
                    });

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onSearchFieldKeyTyped(KeyEvent event) {
        if (searchField.getText().length() > 0) {
            fetchUserSearchHistory();
        }
    }




    @FXML
    private void handleItinerary() {
        // Logique pour afficher ou calculer l'itinéraire
        System.out.println("Itinéraire demandé pour : " + locationNameLabel.getText());

        // Vous pouvez ajouter ici du code pour afficher l'itinéraire dans WebView ou un autre élément
    }

}
