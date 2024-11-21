
package com.example.controller;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import com.example.service.UserService;
import com.example.model.User;
import com.example.database.DatabaseConnection;
import com.example.service.GeolocationService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
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
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private WebView buttonsWebView;
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
    @FXML
    private ScrollPane scrollPane;

    private SparkSession spark;
    private GeolocationService geolocationService;

    @FXML
    private VBox locationVBox;
    @FXML
    private VBox restaurantVBox;

    @FXML
    private VBox pharmacyVBox;

    @FXML
    private VBox mosqueVBox;


    public HomeController() {
        this.geolocationService = new GeolocationService();
    }

    @FXML
    private void hideAllVBoxes() {
        locationVBox.setVisible(false);
        locationVBox.setManaged(false);

        restaurantVBox.setVisible(false);
        restaurantVBox.setManaged(false);

        pharmacyVBox.setVisible(false);
        pharmacyVBox.setManaged(false);

        mosqueVBox.setVisible(false);
        mosqueVBox.setManaged(false);
    }
    @FXML
    private void handleRestaurants(ActionEvent event) {
        showHotels();
        hideAllVBoxes();
        restaurantVBox.setVisible(true);
        restaurantVBox.setManaged(true);

    }
    @FXML
    public void handlePharmacies() {
        // Appeler la méthode pour afficher les pharmacies sur la carte
        showPharmacies();
        hideAllVBoxes();
        pharmacyVBox.setVisible(true);
        pharmacyVBox.setManaged(true);

    }
    @FXML
    public void handlemosque() {
        // Appeler la méthode pour afficher les pharmacies sur la carte
        showMosques();
        hideAllVBoxes();
        mosqueVBox.setVisible(true);
        mosqueVBox.setManaged(true);

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

    public void initialize() {
        if (webView == null) {
            System.err.println("webView est null !");
        } else {
            // Initialisation de la session Spark
            spark = SparkSession.builder()
                    .appName("SearchLocationApp")
                    .master("local")
                    .getOrCreate();

            // Charger le WebView avec le fichier HTML de la carte
            WebEngine webEngine = webView.getEngine();
            String mapFilePath = getClass().getResource("/html/index.html").toExternalForm();

            if (mapFilePath != null) {
                webEngine.load(mapFilePath);
            } else {
                System.err.println("Le fichier HTML de la carte est introuvable !");
            }

            // Ajouter un écouteur pour vérifier quand la carte est prête
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println("Carte prête pour les interactions.");
                    webEngine.executeScript("alert('Carte chargée');"); // Test pour confirmer le chargement
                } else if (newState == Worker.State.FAILED) {
                    System.err.println("Erreur lors du chargement de la carte.");
                }
            });

            // Gestion des erreurs JavaScript
            webEngine.setOnError(event -> System.out.println("Erreur JavaScript : " + event.getMessage()));
            webEngine.setOnAlert(event -> System.out.println(event.getData())); // pour capturer les alertes JavaScript

            System.out.println("Carte chargée : " + webEngine.getLoadWorker().getState());

        }
    }
    public void showMosques() {
        System.out.println("Affichage des mosquées.");

        // Obtenir la localisation actuelle
        GeolocationService geolocationService = new GeolocationService();
        double[] coordinates = geolocationService.getLocationCoordinates();
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        // Modifier le rayon (5000 mètres ici pour tester une zone plus petite)
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:5000," + longitude + "," + latitude + ")[amenity=place_of_worship];out;";

        try {
            // Connexion à l'API Overpass
            URL url = new URL(overpassUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int statusCode = connection.getResponseCode();  // Vérification du code de statut HTTP
            System.out.println("Code de statut HTTP : " + statusCode);

            if (statusCode == 200) {  // Vérifier si la réponse est OK
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Afficher la réponse brute pour vérifier les résultats
                System.out.println("Réponse brute de Overpass : " + response.toString());

                // Analyser la réponse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray elements = jsonResponse.getJSONArray("elements");

                if (elements.length() > 0) {
                    System.out.println("Mosquées trouvées :");

                    // Créer une liste pour les mosquées à envoyer au JavaScript
                    StringBuilder mosqueLocations = new StringBuilder("[");

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject mosque = elements.getJSONObject(i);
                        String name = mosque.optJSONObject("tags").optString("name", "Nom inconnu");
                        double lat = mosque.getDouble("lat");
                        double lon = mosque.getDouble("lon");

                        // Affichage des détails de la mosquée dans la console
                        System.out.println("Nom : " + name);
                        System.out.println("Latitude : " + lat);
                        System.out.println("Longitude : " + lon);
                        System.out.println("------------------------------");

                        // Ajouter les données de la mosquée à la liste au format JSON
                        if (i > 0) {
                            mosqueLocations.append(",");
                        }
                        mosqueLocations.append("{")
                                .append("\"name\": \"").append(name).append("\",")
                                .append("\"lat\": ").append(lat).append(",")
                                .append("\"lng\": ").append(lon)
                                .append("}");
                    }
                    mosqueLocations.append("]");

                    // Si le WebView est initialisé, mettre à jour la carte
                    if (webView != null && webView.getEngine() != null) {
                        webView.getEngine().executeScript("updateMapWithMosques(" + mosqueLocations.toString() + ");");
                        System.out.println("Mosquées envoyées à la carte.");
                    } else {
                        System.out.println("WebView ou son moteur non initialisé.");
                    }

                } else {
                    System.out.println("Aucune mosquée trouvée dans la zone.");
                }
            } else {
                System.out.println("Erreur de connexion avec l'API Overpass, code de statut : " + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     // Référence au VBox défini dans le FXML

    void showHotels() {
        System.out.println("Affichage des hôtels.");

        // Obtenir la localisation actuelle
        GeolocationService geolocationService = new GeolocationService();
        double[] coordinates = geolocationService.getLocationCoordinates();
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        // Réduire le rayon pour une recherche plus précise (5 km)
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:5000," + longitude + "," + latitude + ")[tourism=hotel];out;";

        try {
            // Connexion à l'API Overpass
            URL url = new URL(overpassUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Afficher la réponse brute pour vérifier les résultats
            System.out.println("Réponse brute de Overpass : " + response.toString());

            // Analyser la réponse JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray elements = jsonResponse.getJSONArray("elements");

            if (elements.length() > 0) {
                System.out.println("Hôtels trouvés :");

                // Créer une liste pour les hôtels à envoyer au JavaScript
                StringBuilder hotelLocations = new StringBuilder("[");

                // Créer des HBox dynamiques pour afficher les hôtels dans le VBox
                for (int i = 0; i < elements.length(); i++) {
                    JSONObject hotel = elements.getJSONObject(i);

                    String name = hotel.optJSONObject("tags").optString("name", "Nom inconnu");
                    double lat = hotel.getDouble("lat");
                    double lon = hotel.getDouble("lon");

                    // Affichage des détails de l'hôtel dans la console
                    System.out.println("Nom : " + name);
                    System.out.println("Latitude : " + lat);
                    System.out.println("Longitude : " + lon);
                    System.out.println("------------------------------");

                    // Ajouter les données de l'hôtel à la liste au format JSON
                    if (i > 0) {
                        hotelLocations.append(",");
                    }
                    hotelLocations.append("{")
                            .append("\"name\": \"").append(name).append("\",")
                            .append("\"lat\": ").append(lat).append(",")
                            .append("\"lng\": ").append(lon)
                            .append("}");

                    // Ajouter dynamiquement un HBox pour chaque hôtel dans le VBox
                    HBox hotelHBox = new HBox(10); // Espacement de 10 pixels entre les éléments
                    hotelHBox.setAlignment(Pos.CENTER_LEFT); // Alignement à gauche
                    hotelHBox.setStyle("-fx-padding: 5;"); // Style de padding

                    Label hotelLabel = new Label(name + " - (" + lat + ", " + lon + ")");
                    hotelLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Style de texte

                    hotelHBox.getChildren().add(hotelLabel); // Ajouter le label à l'HBox

                    // Ajouter l'HBox au VBox défini dans le FXML (restaurantVBox)
                    if (restaurantVBox != null) {
                        restaurantVBox.getChildren().add(hotelHBox);
                    }

                }

                hotelLocations.append("]");

                // Ajouter les marqueurs sur la carte via WebView
                if (webView != null && webView.getEngine() != null) {
                    webView.getEngine().executeScript("updateMapWithHotels(" + hotelLocations.toString() + ");");
                    System.out.println("Hôtels envoyés à la carte.");
                } else {
                    System.out.println("WebView ou son moteur non initialisé.");
                }

            } else {
                System.out.println("Aucun hôtel trouvé dans la zone.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showPharmacies() {
        System.out.println("Affichage des pharmacies.");

        // Obtenir la localisation actuelle
        GeolocationService geolocationService = new GeolocationService();
        double[] coordinates = geolocationService.getLocationCoordinates();
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        // Modifier le rayon (5000 mètres ici pour tester une zone plus petite)
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:5000," + longitude + "," + latitude + ")[amenity=pharmacy];out;";

        try {
            // Connexion à l'API Overpass
            URL url = new URL(overpassUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int statusCode = connection.getResponseCode();  // Vérification du code de statut HTTP
            System.out.println("Code de statut HTTP : " + statusCode);

            if (statusCode == 200) {  // Vérifier si la réponse est OK
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Afficher la réponse brute pour vérifier les résultats
                System.out.println("Réponse brute de Overpass : " + response.toString());

                // Analyser la réponse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray elements = jsonResponse.getJSONArray("elements");

                if (elements.length() > 0) {
                    System.out.println("Pharmacies trouvées :");

                    // Créer une liste pour les pharmacies à envoyer au JavaScript
                    StringBuilder pharmacyLocations = new StringBuilder("[");

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject pharmacy = elements.getJSONObject(i);
                        String name = pharmacy.optJSONObject("tags").optString("name", "Nom inconnu");
                        double lat = pharmacy.getDouble("lat");
                        double lon = pharmacy.getDouble("lon");

                        // Affichage des détails de la pharmacie dans la console
                        System.out.println("Nom : " + name);
                        System.out.println("Latitude : " + lat);
                        System.out.println("Longitude : " + lon);
                        System.out.println("------------------------------");

                        // Ajouter les données de la pharmacie à la liste au format JSON
                        if (i > 0) {
                            pharmacyLocations.append(",");
                        }
                        pharmacyLocations.append("{")
                                .append("\"name\": \"").append(name).append("\",")
                                .append("\"lat\": ").append(lat).append(",")
                                .append("\"lng\": ").append(lon)
                                .append("}");
                    }
                    pharmacyLocations.append("]");

                    // Si le WebView est initialisé, mettre à jour la carte
                    if (webView != null && webView.getEngine() != null) {
                        webView.getEngine().executeScript("updateMapWithPharmacies(" + pharmacyLocations.toString() + ");");
                        System.out.println("Pharmacies envoyées à la carte.");
                    } else {
                        System.out.println("WebView ou son moteur non initialisé.");
                    }

                } else {
                    System.out.println("Aucune pharmacie trouvée dans la zone.");
                }
            } else {
                System.out.println("Erreur de connexion avec l'API Overpass, code de statut : " + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            searchLocation(searchQuery); // Appeler la méthode pour effectuer la recherche

            hideAllVBoxes();
            locationVBox.setVisible(true);
            locationVBox.setManaged(true);
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
                        String location = resultSet.getString("name");
                        history.add(location);
                        System.out.println("Lieu recherché : " + location); // Affiche chaque lieu dans la console
                    }

                    // Optionnel : Afficher tout l'historique une fois collecté
                    System.out.println("Historique des recherches pour l'utilisateur : " + history);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void onSearchFieldKeyTyped(KeyEvent event) {
        String searchText = searchField.getText().trim();

        // Si le texte est assez long (plus de 2 caractères, par exemple), on affiche les suggestions
        if (searchText.length() > 2) {
            fetchUserSearchHistory();
        }
    }




    @FXML
    private void handleItinerary() {
        // Logique pour afficher ou calculer l'itinéraire
        System.out.println("Itinéraire demandé pour : " + locationNameLabel.getText());

        // Vous pouvez ajouter ici du code pour afficher l'itinéraire dans WebView ou un autre élément
    }

    @FXML
    private void handleSave() {
        String name = locationNameLabel.getText();


        storeLocationInDatabase(name);
    }

    private void storeLocationInDatabase(String name) {
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

            // Insérer une nouvelle entrée dans la table locations
            String insertQuery = "INSERT INTO locations (NAME, USER_ID) VALUES (?, ?)";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

                // Définir les paramètres de la requête
                preparedStatement.setString(1, name);  // 1er paramètre : nom de la location
                preparedStatement.setInt(2, userId);   // 2ème paramètre : ID de l'utilisateur

                // Exécuter la requête d'insertion
                preparedStatement.executeUpdate();
                System.out.println("Location enregistrée : " + name + " pour l'utilisateur ID : " + userId);

            } catch (SQLException e) {
                System.err.println("Erreur lors de l'enregistrement de la location : " + e.getMessage());
            }
        } else {
            System.err.println("L'utilisateur n'existe pas dans la base de données.");
        }
    }
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

    public void handleTransports(ActionEvent actionEvent) {
    }
}


