
package com.example.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.util.shapes.GHPoint;
import com.sun.webkit.WebPage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import com.example.service.UserService;
import com.example.model.User;
import com.example.database.DatabaseConnection;
import com.example.service.GeolocationService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apache.http.HttpEntity;
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

import javax.swing.*;
import java.awt.*;
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
import java.util.*;
import java.util.List;

public class HomeController {
    @FXML
    public Label mosqueLabel;
    @FXML
    public Button savButton;
    @FXML
    public Button routeButton;
    public HBox mosqueHBox;
    public ScrollPane enregistreScrollPane;
    public VBox enregistreVBox;
    public ScrollPane profileScrollPane;
    public VBox profileVBox;
    @FXML
    private  ScrollPane recherchrecenteScrollPane;
    @FXML
    private Button logoutButton;
    public ScrollPane transportScrollPane;
    public VBox transportVBox;
    public VBox  recentSearchesVBox;
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
    private Label loadingLabel;
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
    private ScrollPane restaurantScrollPane;
    @FXML
    private ScrollPane mosqueScrollPane;
    @FXML
    private ScrollPane pharmacyScrollPane;
    @FXML
    private VBox locationVBox;
    @FXML
    private VBox restaurantVBox;
    @FXML
    private VBox pharmacyVBox;
    @FXML
    private VBox mosqueVBox;
    @FXML
    private Label carDurationLabel;
    @FXML
    private Label bikeDurationLabel;
    @FXML
    private Label walkDurationLabel;

    private WebEngine webEngine;

    public HomeController() {
        this.geolocationService = new GeolocationService();
    }

    // Déclaration des champs pour la position de départ et d'arrivée
    @FXML
    private VBox itineraryFieldsVBox;

    @FXML
    private TextField startField;

    @FXML
    private TextField endField;
    @FXML
    private ProgressIndicator loadingIndicator;

    // Méthode qui sera appelée lorsque l'utilisateur clique sur le bouton "Itinéraire"




    @FXML
    private void hideAllVBoxes() {
        locationVBox.setVisible(false);
        locationVBox.setManaged(false);

        itineraryFieldsVBox.setVisible(false);
        itineraryFieldsVBox.setManaged(false);

        restaurantScrollPane.setVisible(false);
        restaurantScrollPane.setManaged(false);

        mosqueScrollPane.setVisible(false);
        mosqueScrollPane.setManaged(false);

        pharmacyScrollPane.setVisible(false);
        pharmacyScrollPane.setManaged(false);

        transportScrollPane.setVisible(false);
        transportScrollPane.setManaged(false);

        recherchrecenteScrollPane.setVisible(false);
        recherchrecenteScrollPane.setManaged(false);

        enregistreScrollPane.setVisible(false);
        enregistreScrollPane.setManaged(false);

        profileScrollPane.setVisible(false);
        profileScrollPane.setManaged(false);
    }

    @FXML
    private void handleItinerary() {
        // Cacher toutes les autres VBoxes
        hideAllVBoxes();

        // Afficher les champs de l'itinéraire
        itineraryFieldsVBox.setVisible(true);
        itineraryFieldsVBox.setManaged(true);
    }
    @FXML
    private void handleRestaurants(ActionEvent event) {
        showHotels();  // Cette méthode affiche les hôtels sur la carte
        hideAllVBoxes();  // Masque tous les VBox et ScrollPanes

        // Rendre visible le ScrollPane du restaurant (et donc son VBox)
        restaurantScrollPane.setVisible(true);
        restaurantScrollPane.setManaged(true);
    }
    @FXML
    public void handleTransports(ActionEvent actionEvent) {
        showTransports();  // Cette méthode affiche les hôtels sur la carte
        hideAllVBoxes();  // Masque tous les VBox et ScrollPanes

        // Rendre visible le ScrollPane du restaurant (et donc son VBox)
        transportScrollPane.setVisible(true);
        transportScrollPane.setManaged(true);

    }
    @FXML
    public void handlePharmacies() {
        showPharmacies();  // Cette méthode affiche les pharmacies sur la carte
        hideAllVBoxes();  // Masque tous les VBox et ScrollPanes

        // Rendre visible le ScrollPane de la pharmacie (et donc son VBox)
        pharmacyScrollPane.setVisible(true);
        pharmacyScrollPane.setManaged(true);
    }

    @FXML
    public void handlemosque() {
        showMosques();  // Cette méthode affiche les mosquées sur la carte
        hideAllVBoxes();  // Masque tous les VBox et ScrollPanes

        // Rendre visible le ScrollPane de la mosquée (et donc son VBox)
        mosqueScrollPane.setVisible(true);
        mosqueScrollPane.setManaged(true);
    }

    private void getLocationNameFromCoordinates(double latitude, double longitude) {
        try {
            // Encodage des paramètres pour éviter des problèmes avec les caractères spéciaux
            String lat = URLEncoder.encode(String.valueOf(longitude), StandardCharsets.UTF_8);  // Inverser ici
            String lon = URLEncoder.encode(String.valueOf(latitude), StandardCharsets.UTF_8);   // Inverser ici

            // Utilisation de l'API Nominatim sans clé API
            String apiUrl = String.format("https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json&addressdetails=1", lat, lon);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        try {
                            System.out.println("Réponse JSON brute : " + response); // Afficher la réponse brute pour le débogage

                            // Essayer d'analyser la réponse JSON
                            JSONObject json = new JSONObject(response);

                            // Extraire le nom de la ville depuis l'objet "address"
                            String city = json.optJSONObject("address").optString("city", "Ville introuvable");

                            if ("Ville introuvable".equals(city)) {
                                System.err.println("Erreur : Ville introuvable.");
                            } else {
                                System.out.println("Ville : " + city);

                                // Appeler la fonction Wikipedia avec le nom de la ville
                                getWikipediaDetails(city);
                            }

                        } catch (JSONException e) {
                            System.err.println("Erreur lors de l'analyse du JSON : " + e.getMessage());
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("Erreur lors de la requête HTTP : " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Erreur lors de la construction de l'URL : " + e.getMessage());
        }
    }


    public void handleGetLocation() {
        // Récupérer les coordonnées
        double[] coordinates = geolocationService.getLocationCoordinates();

        if (coordinates != null && coordinates.length == 2) {
            // Inverser latitude et longitude dans le JSON
            String locationsJson = String.format(
                    Locale.US,
                    "[{\"lat\": %f, \"lng\": %f}]",
                    coordinates[1],  // longitude devient latitude
                    coordinates[0]   // latitude devient longitude
            );

            // Générer le script JavaScript
            String script = String.format("updateMap(%s);", locationsJson);

            // Journaux pour vérifier les valeurs
            System.out.println("Longitude : " + coordinates[1] + ", Latitude: " + coordinates[0]);
            System.out.println("JSON généré (inversé) : " + locationsJson);
            System.out.println("Script exécuté : " + script);

            try {
                // Exécuter le script dans le WebView
                webView.getEngine().executeScript(script);

                // Tester la communication avec une alerte JavaScript
                webView.getEngine().executeScript("alert('Test JavaScript depuis Java');");

                // Appeler la fonction pour récupérer le nom du lieu en utilisant le géocodage inverse
                getLocationNameFromCoordinates(coordinates[0], coordinates[1]);

                // Masquer tous les VBox et afficher celui des informations de localisation
                hideAllVBoxes();
                locationVBox.setVisible(true);
                locationVBox.setManaged(true);

            } catch (Exception e) {
                System.err.println("Erreur lors de l'exécution du script : " + e.getMessage());
                e.printStackTrace();
            }
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



    void showTransports() {
        System.out.println("Affichage des Transports.");

        if (webView != null && webView.getEngine() != null) {
            Object markersArrayCheck = webView.getEngine().executeScript("Array.isArray(markersArray)");

            if (markersArrayCheck != null && (Boolean) markersArrayCheck) {
                Boolean isMarkersArrayEmpty = (Boolean) webView.getEngine().executeScript("markersArray.length === 0");

                if (isMarkersArrayEmpty) {
                    System.out.println("Tableau de marqueurs vide, géolocalisation...");
                    GeolocationService geolocationService = new GeolocationService();
                    double[] coordinates = geolocationService.getLocationCoordinates();
                    double latitude = coordinates[0];
                    double longitude = coordinates[1];

                    System.out.println("Coordonnées de géolocalisation : Latitude = " + latitude + ", Longitude = " + longitude);
                    fetchTransportsAroundLocation(latitude, longitude);
                } else {
                    System.out.println("Tableau de marqueurs non vide, utilisation des coordonnées du premier marqueur...");

                    String script = "markersArray[0];";
                    Object firstMarkerCoordinates = webView.getEngine().executeScript(script);

                    if (firstMarkerCoordinates != null) {
                        System.out.println("Coordonnées du premier marqueur récupérées : " + firstMarkerCoordinates);

                        // Vérifier si c'est un JSObject
                        if (firstMarkerCoordinates instanceof JSObject) {
                            JSObject marker = (JSObject) firstMarkerCoordinates;

                            // Extraire les coordonnées (latitude et longitude)
                            Object latitudeObj = marker.getMember("lat");  // supposons que "lat" est le nom de la latitude
                            Object longitudeObj = marker.getMember("lng"); // supposons que "lng" est le nom de la longitude

                            if (latitudeObj instanceof Double && longitudeObj instanceof Double) {
                                double latitude = (Double) latitudeObj;
                                double longitude = (Double) longitudeObj;

                                System.out.println("Coordonnées du premier marqueur : Latitude = " + latitude + ", Longitude = " + longitude);

                                fetchTransportsAroundLocation( longitude,latitude);
                            } else {
                                System.out.println("Les coordonnées du marqueur ne sont pas dans le format attendu.");
                            }
                        } else {
                            System.out.println("Les coordonnées du premier marqueur ne sont pas dans un objet valide.");
                        }
                    } else {
                        System.out.println("Erreur lors de la récupération des coordonnées du premier marqueur.");
                    }
                }
            } else {
                System.out.println("markersArray n'est pas un tableau valide.");
            }
        } else {
            System.out.println("WebView ou son moteur non initialisé.");
        }
    }

    private void fetchTransportsAroundLocation(double latitude, double longitude) {
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];(node(around:20000," + longitude + "," + latitude + ")[amenity=taxi];node(around:20000," + latitude + "," + longitude + ")[railway=tram_stop];node(around:20000," + latitude + "," + longitude + ")[highway=bus_stop];);out;";

        try {
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

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray elements = jsonResponse.getJSONArray("elements");

            if (elements.length() > 0) {
                System.out.println("Transports trouvés :");

                StringBuilder transportsLocations = new StringBuilder("[");

                if (transportVBox != null) {
                    transportVBox.getChildren().clear();
                }

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject transport = elements.getJSONObject(i);

                    JSONObject tags = transport.optJSONObject("tags");
                    String name = tags != null ? tags.optString("name", "Nom inconnu") : "Nom inconnu";

                    if (name.equals("Nom inconnu")) {
                        continue;
                    }

                    StringBuilder transportInfo = new StringBuilder();
                    if (tags != null) {
                        for (String key : tags.keySet()) {
                            if (!key.equals("amenity") && !key.equals("name")) {
                                String value = tags.optString(key, "Non disponible");
                                transportInfo.append(key).append(": ").append(value).append("\n");
                            }
                        }
                    }

                    if (i > 0) {
                        transportsLocations.append(",");
                    }
                    transportsLocations.append("{")
                            .append("\"name\": \"").append(name).append("\",")
                            .append("\"info\": \"").append(transportInfo.toString().replaceAll("\n", " | ")).append("\",")
                            .append("\"lat\": ").append(transport.optDouble("lat")).append(",")
                            .append("\"lng\": ").append(transport.optDouble("lon"))
                            .append("}");

                    HBox transportHBox = new HBox(8);
                    transportHBox.setAlignment(Pos.CENTER_LEFT);
                    transportHBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

                    Label transportLabel = new Label(name + "\n" + transportInfo.toString());
                    transportLabel.setWrapText(true);
                    transportLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-max-width: 500px; -fx-text-fill: #333;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Bouton "Enregistrer" avec style circulaire
                    Button saveButton = new Button();
                    Image saveImage = new Image(getClass().getResource("/images/save-icon.png").toString());
                    ImageView saveImageView = new ImageView(saveImage);
                    saveImageView.setFitWidth(20);
                    saveImageView.setFitHeight(20);
                    saveButton.setGraphic(saveImageView);
                    saveButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        // Logique d'enregistrement ici
                    });

                    // Bouton "Itinéraire" avec style circulaire
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        // Logique de calcul d'itinéraire ici
                    });

                    VBox buttonsBox = new VBox(10, saveButton, routeButton);
                    buttonsBox.setAlignment(Pos.TOP_RIGHT);

                    transportHBox.getChildren().addAll(transportLabel, spacer, buttonsBox);

                    if (transportVBox != null) {
                        transportVBox.getChildren().add(transportHBox);
                    }
                }

                transportsLocations.append("]");

                if (webView != null && webView.getEngine() != null) {
                    webView.getEngine().executeScript("updateMapWithTransports(" + transportsLocations.toString() + ");");
                    System.out.println("Transports envoyés à la carte.");
                } else {
                    System.out.println("WebView ou son moteur non initialisé.");
                }
            } else {
                System.out.println("Aucun transport trouvé dans la zone.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    void showHotels() {
        System.out.println("Affichage des hôtels.");

        if (webView != null && webView.getEngine() != null) {
            Object markersArrayCheck = webView.getEngine().executeScript("Array.isArray(markersArray)");

            if (markersArrayCheck != null && (Boolean) markersArrayCheck) {
                Boolean isMarkersArrayEmpty = (Boolean) webView.getEngine().executeScript("markersArray.length === 0");

                if (isMarkersArrayEmpty) {
                    System.out.println("Tableau de marqueurs vide, géolocalisation...");
                    GeolocationService geolocationService = new GeolocationService();
                    double[] coordinates = geolocationService.getLocationCoordinates();
                    double latitude = coordinates[0];
                    double longitude = coordinates[1];

                    System.out.println("Coordonnées de géolocalisation : Latitude = " + latitude + ", Longitude = " + longitude);
                    fetchHotelsAroundLocation(latitude, longitude);
                } else {
                    System.out.println("Tableau de marqueurs non vide, utilisation des coordonnées du premier marqueur...");

                    String script = "markersArray[0];";
                    Object firstMarkerCoordinates = webView.getEngine().executeScript(script);

                    if (firstMarkerCoordinates != null) {
                        System.out.println("Coordonnées du premier marqueur récupérées : " + firstMarkerCoordinates);

                        // Vérifier si c'est un JSObject
                        if (firstMarkerCoordinates instanceof JSObject) {
                            JSObject marker = (JSObject) firstMarkerCoordinates;

                            // Extraire les coordonnées (latitude et longitude)
                            Object latitudeObj = marker.getMember("lat");  // supposons que "lat" est le nom de la latitude
                            Object longitudeObj = marker.getMember("lng"); // supposons que "lng" est le nom de la longitude

                            if (latitudeObj instanceof Double && longitudeObj instanceof Double) {
                                double latitude = (Double) latitudeObj;
                                double longitude = (Double) longitudeObj;

                                System.out.println("Coordonnées du premier marqueur : Latitude = " + latitude + ", Longitude = " + longitude);

                                fetchHotelsAroundLocation( longitude,latitude);
                            } else {
                                System.out.println("Les coordonnées du marqueur ne sont pas dans le format attendu.");
                            }
                        } else {
                            System.out.println("Les coordonnées du premier marqueur ne sont pas dans un objet valide.");
                        }
                    } else {
                        System.out.println("Erreur lors de la récupération des coordonnées du premier marqueur.");
                    }
                }
            } else {
                System.out.println("markersArray n'est pas un tableau valide.");
            }
        } else {
            System.out.println("WebView ou son moteur non initialisé.");
        }
    }
    private void fetchHotelsAroundLocation(double latitude, double longitude) {
        // Réduire le rayon pour une recherche plus précise (5 km)
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:5000," + longitude + "," + latitude + ")[tourism=hotel];out;";

        try {
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

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray elements = jsonResponse.getJSONArray("elements");

            if (elements.length() > 0) {
                System.out.println("Hotels trouvées :");

                StringBuilder hotelsLocations = new StringBuilder("[");

                if (restaurantVBox != null) {
                    restaurantVBox.getChildren().clear();
                }

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject hotel = elements.getJSONObject(i);

                    JSONObject tags = hotel.optJSONObject("tags");
                    String name = tags != null ? tags.optString("name", "Nom inconnu") : "Nom inconnu";

                    if (name.equals("Nom inconnu")) {
                        continue;
                    }

                    StringBuilder hotelInfo = new StringBuilder();
                    if (tags != null) {
                        for (String key : tags.keySet()) {
                            if (!key.equals("amenity") && !key.equals("name")) {
                                String value = tags.optString(key, "Non disponible");
                                hotelInfo.append(key).append(": ").append(value).append("\n");
                            }
                        }
                    }

                    if (i > 0) {
                        hotelsLocations.append(",");
                    }
                    hotelsLocations.append("{")
                            .append("\"name\": \"").append(name).append("\",")
                            .append("\"info\": \"").append(hotelInfo.toString().replaceAll("\n", " | ")).append("\",")
                            .append("\"lat\": ").append(hotel.optDouble("lat")).append(",")
                            .append("\"lng\": ").append(hotel.optDouble("lon"))
                            .append("}");

                    // Créer un HBox pour afficher les informations
                    HBox hotelHBox = new HBox(8);
                    hotelHBox.setAlignment(Pos.TOP_LEFT);
                    hotelHBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

                    // Ajoutez toutes les informations sous forme de texte
                    Text hotelText = new Text("Nom : " + name + "\nInformations : \n" + hotelInfo.toString());
                    hotelText.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                    hotelText.setWrappingWidth(400); // Limitez la largeur pour éviter des débordements

                    // Créer un HBox pour les boutons et les aligner en bas
                    VBox buttonsBox = new VBox(10);
                    buttonsBox.setAlignment(Pos.TOP_LEFT); // Les boutons seront à gauche sous le texte

                    // Bouton "Enregistrer"
                    Button saveButton = new Button();
                    Image saveImage = new Image(getClass().getResource("/images/save-icon.png").toString());
                    ImageView saveImageView = new ImageView(saveImage);
                    saveImageView.setFitWidth(20);
                    saveImageView.setFitHeight(20);
                    saveButton.setGraphic(saveImageView);
                    saveButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        // Logique d'enregistrement ici
                    });

                    // Bouton "Itinéraire"
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        // Logique de calcul d'itinéraire ici
                    });

                    // Ajoutez les boutons à la VBox
                    buttonsBox.getChildren().addAll(saveButton, routeButton);

                    // Ajoutez le texte et les boutons dans le HBox
                    hotelHBox.getChildren().addAll(hotelText, buttonsBox);

                    // Ajoutez le HBox à la VBox principale
                    if (restaurantVBox != null) {
                        restaurantVBox.getChildren().add(hotelHBox);
                    }
                }

                hotelsLocations.append("]");

                if (webView != null && webView.getEngine() != null) {
                    webView.getEngine().executeScript("updateMapWithRestaurants(" + hotelsLocations.toString() + ");");
                    System.out.println("Pharmacies envoyées à la carte.");
                } else {
                    System.out.println("WebView ou son moteur non initialisé.");
                }
            } else {
                System.out.println("Aucune pharmacie trouvée dans la zone.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    void showPharmacies() {
        System.out.println("Affichage des pharmacies.");

        if (webView != null && webView.getEngine() != null) {
            Object markersArrayCheck = webView.getEngine().executeScript("Array.isArray(markersArray)");

            if (markersArrayCheck != null && (Boolean) markersArrayCheck) {
                Boolean isMarkersArrayEmpty = (Boolean) webView.getEngine().executeScript("markersArray.length === 0");

                if (isMarkersArrayEmpty) {
                    System.out.println("Tableau de marqueurs vide, géolocalisation...");
                    GeolocationService geolocationService = new GeolocationService();
                    double[] coordinates = geolocationService.getLocationCoordinates();
                    double latitude = coordinates[0];
                    double longitude = coordinates[1];

                    System.out.println("Coordonnées de géolocalisation : Latitude = " + latitude + ", Longitude = " + longitude);
                    fetchpharmaciesAroundLocation(latitude, longitude);
                } else {
                    System.out.println("Tableau de marqueurs non vide, utilisation des coordonnées du premier marqueur...");

                    String script = "markersArray[0];";
                    Object firstMarkerCoordinates = webView.getEngine().executeScript(script);

                    if (firstMarkerCoordinates != null) {
                        System.out.println("Coordonnées du premier marqueur récupérées : " + firstMarkerCoordinates);

                        // Vérifier si c'est un JSObject
                        if (firstMarkerCoordinates instanceof JSObject) {
                            JSObject marker = (JSObject) firstMarkerCoordinates;

                            // Extraire les coordonnées (latitude et longitude)
                            Object latitudeObj = marker.getMember("lat");  // supposons que "lat" est le nom de la latitude
                            Object longitudeObj = marker.getMember("lng"); // supposons que "lng" est le nom de la longitude

                            if (latitudeObj instanceof Double && longitudeObj instanceof Double) {
                                double latitude = (Double) latitudeObj;
                                double longitude = (Double) longitudeObj;

                                System.out.println("Coordonnées du premier marqueur : Latitude = " + latitude + ", Longitude = " + longitude);

                                fetchpharmaciesAroundLocation( longitude,latitude);
                            } else {
                                System.out.println("Les coordonnées du marqueur ne sont pas dans le format attendu.");
                            }
                        } else {
                            System.out.println("Les coordonnées du premier marqueur ne sont pas dans un objet valide.");
                        }
                    } else {
                        System.out.println("Erreur lors de la récupération des coordonnées du premier marqueur.");
                    }
                }
            } else {
                System.out.println("markersArray n'est pas un tableau valide.");
            }
        } else {
            System.out.println("WebView ou son moteur non initialisé.");
        }
    }
    private void fetchpharmaciesAroundLocation(double latitude, double longitude) {
        // Réduire le rayon pour une recherche plus précise (5 km)
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:5000," + longitude + "," + latitude + ")[amenity=pharmacy];out;";

        try {
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

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray elements = jsonResponse.getJSONArray("elements");

            if (elements.length() > 0) {
                System.out.println("Pharmacies trouvées :");

                StringBuilder pharmaciesLocations = new StringBuilder("[");

                if (pharmacyVBox != null) {
                    pharmacyVBox.getChildren().clear();
                }

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject pharmacy = elements.getJSONObject(i);

                    JSONObject tags = pharmacy.optJSONObject("tags");
                    String name = tags != null ? tags.optString("name", "Nom inconnu") : "Nom inconnu";

                    if (name.equals("Nom inconnu")) {
                        continue;
                    }

                    StringBuilder pharmacyInfo = new StringBuilder();
                    if (tags != null) {
                        for (String key : tags.keySet()) {
                            if (!key.equals("amenity") && !key.equals("name")) {
                                String value = tags.optString(key, "Non disponible");
                                pharmacyInfo.append(key).append(": ").append(value).append("\n");
                            }
                        }
                    }

                    if (i > 0) {
                        pharmaciesLocations.append(",");
                    }
                    pharmaciesLocations.append("{")
                            .append("\"name\": \"").append(name).append("\",")
                            .append("\"info\": \"").append(pharmacyInfo.toString().replaceAll("\n", " | ")).append("\",")
                            .append("\"lat\": ").append(pharmacy.optDouble("lat")).append(",")
                            .append("\"lng\": ").append(pharmacy.optDouble("lon"))
                            .append("}");

                    // Créer un HBox pour afficher les informations
                    HBox pharmacyHBox = new HBox(8);
                    pharmacyHBox.setAlignment(Pos.TOP_LEFT);
                    pharmacyHBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

                    // Ajoutez toutes les informations sous forme de texte
                    Text pharmacyText = new Text("Nom : " + name + "\nInformations : \n" + pharmacyInfo.toString());
                    pharmacyText.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                    pharmacyText.setWrappingWidth(400); // Limitez la largeur pour éviter des débordements

                    // Créer un HBox pour les boutons et les aligner en bas
                    VBox buttonsBox = new VBox(10);
                    buttonsBox.setAlignment(Pos.TOP_LEFT); // Les boutons seront à gauche sous le texte

                    // Bouton "Enregistrer"
                    Button saveButton = new Button();
                    Image saveImage = new Image(getClass().getResource("/images/save-icon.png").toString());
                    ImageView saveImageView = new ImageView(saveImage);
                    saveImageView.setFitWidth(20);
                    saveImageView.setFitHeight(20);
                    saveButton.setGraphic(saveImageView);
                    saveButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        // Logique d'enregistrement ici
                    });

                    // Bouton "Itinéraire"
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        // Logique de calcul d'itinéraire ici
                    });

                    // Ajoutez les boutons à la VBox
                    buttonsBox.getChildren().addAll(saveButton, routeButton);

                    // Ajoutez le texte et les boutons dans le HBox
                    pharmacyHBox.getChildren().addAll(pharmacyText, buttonsBox);

                    // Ajoutez le HBox à la VBox principale
                    if (pharmacyVBox != null) {
                        pharmacyVBox.getChildren().add(pharmacyHBox);
                    }
                }

                pharmaciesLocations.append("]");

                if (webView != null && webView.getEngine() != null) {
                    webView.getEngine().executeScript("updateMapWithPharmacies(" + pharmaciesLocations.toString() + ");");
                    System.out.println("Pharmacies envoyées à la carte.");
                } else {
                    System.out.println("WebView ou son moteur non initialisé.");
                }
            } else {
                System.out.println("Aucune pharmacie trouvée dans la zone.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void showMosques() {
        System.out.println("Affichage des mosques.");

        if (webView != null && webView.getEngine() != null) {
            Object markersArrayCheck = webView.getEngine().executeScript("Array.isArray(markersArray)");

            if (markersArrayCheck != null && (Boolean) markersArrayCheck) {
                Boolean isMarkersArrayEmpty = (Boolean) webView.getEngine().executeScript("markersArray.length === 0");

                if (isMarkersArrayEmpty) {
                    System.out.println("Tableau de marqueurs vide, géolocalisation...");
                    GeolocationService geolocationService = new GeolocationService();
                    double[] coordinates = geolocationService.getLocationCoordinates();
                    double latitude = coordinates[0];
                    double longitude = coordinates[1];

                    System.out.println("Coordonnées de géolocalisation : Latitude = " + latitude + ", Longitude = " + longitude);
                    fetchmosquesAroundLocation(latitude, longitude);
                } else {
                    System.out.println("Tableau de marqueurs non vide, utilisation des coordonnées du premier marqueur...");

                    String script = "markersArray[0];";
                    Object firstMarkerCoordinates = webView.getEngine().executeScript(script);

                    if (firstMarkerCoordinates != null) {
                        System.out.println("Coordonnées du premier marqueur récupérées : " + firstMarkerCoordinates);

                        // Vérifier si c'est un JSObject
                        if (firstMarkerCoordinates instanceof JSObject) {
                            JSObject marker = (JSObject) firstMarkerCoordinates;

                            // Extraire les coordonnées (latitude et longitude)
                            Object latitudeObj = marker.getMember("lat");  // supposons que "lat" est le nom de la latitude
                            Object longitudeObj = marker.getMember("lng"); // supposons que "lng" est le nom de la longitude

                            if (latitudeObj instanceof Double && longitudeObj instanceof Double) {
                                double latitude = (Double) latitudeObj;
                                double longitude = (Double) longitudeObj;

                                System.out.println("Coordonnées du premier marqueur : Latitude = " + latitude + ", Longitude = " + longitude);

                                fetchmosquesAroundLocation( longitude,latitude);
                            } else {
                                System.out.println("Les coordonnées du marqueur ne sont pas dans le format attendu.");
                            }
                        } else {
                            System.out.println("Les coordonnées du premier marqueur ne sont pas dans un objet valide.");
                        }
                    } else {
                        System.out.println("Erreur lors de la récupération des coordonnées du premier marqueur.");
                    }
                }
            } else {
                System.out.println("markersArray n'est pas un tableau valide.");
            }
        } else {
            System.out.println("WebView ou son moteur non initialisé.");
        }
    }
    private void fetchmosquesAroundLocation(double latitude, double longitude) {
        // Réduire le rayon pour une recherche plus précise (5 km)
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:5000," + longitude + "," + latitude + ")[amenity=place_of_worship];out;";
        try {
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

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray elements = jsonResponse.getJSONArray("elements");

            if (elements.length() > 0) {
                System.out.println("Mosques trouvées :");

                StringBuilder mosquesLocations = new StringBuilder("[");

                if (mosqueVBox != null) {
                    mosqueVBox.getChildren().clear();
                }

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject mosque = elements.getJSONObject(i);

                    JSONObject tags = mosque.optJSONObject("tags");
                    String name = tags != null ? tags.optString("name", "Nom inconnu") : "Nom inconnu";

                    if (name.equals("Nom inconnu")) {
                        continue;
                    }

                    StringBuilder mosqueInfo = new StringBuilder();
                    if (tags != null) {
                        for (String key : tags.keySet()) {
                            if (!key.equals("amenity") && !key.equals("name")) {
                                String value = tags.optString(key, "Non disponible");
                                mosqueInfo.append(key).append(": ").append(value).append("\n");
                            }
                        }
                    }

                    if (i > 0) {
                        mosquesLocations.append(",");
                    }
                    mosquesLocations.append("{")
                            .append("\"name\": \"").append(name).append("\",")
                            .append("\"info\": \"").append(mosqueInfo.toString().replaceAll("\n", " | ")).append("\",")
                            .append("\"lat\": ").append(mosque.optDouble("lat")).append(",")
                            .append("\"lng\": ").append(mosque.optDouble("lon"))
                            .append("}");

                    // Créer un HBox pour afficher les informations
                    HBox mosqueHBox = new HBox(8);
                    mosqueHBox.setAlignment(Pos.TOP_LEFT);
                    mosqueHBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

                    // Ajoutez toutes les informations sous forme de texte
                    Text mosqueText = new Text("Nom : " + name + "\nInformations : \n" + mosqueInfo.toString());
                    mosqueText.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                    mosqueText.setWrappingWidth(400); // Limitez la largeur pour éviter des débordements

                    // Créer un HBox pour les boutons et les aligner en bas
                    VBox buttonsBox = new VBox(10);
                    buttonsBox.setAlignment(Pos.TOP_LEFT); // Les boutons seront à gauche sous le texte

                    // Bouton "Enregistrer"
                    Button saveButton = new Button();
                    Image saveImage = new Image(getClass().getResource("/images/save-icon.png").toString());
                    ImageView saveImageView = new ImageView(saveImage);
                    saveImageView.setFitWidth(20);
                    saveImageView.setFitHeight(20);
                    saveButton.setGraphic(saveImageView);
                    saveButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        // Logique d'enregistrement ici
                    });

                    // Bouton "Itinéraire"
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-background-color: #f9f9f9; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-border-color: #ccc; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 50%; " +
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        // Logique de calcul d'itinéraire ici
                    });

                    // Ajoutez les boutons à la VBox
                    buttonsBox.getChildren().addAll(saveButton, routeButton);

                    // Ajoutez le texte et les boutons dans le HBox
                    mosqueHBox.getChildren().addAll(mosqueText, buttonsBox);

                    // Ajoutez le HBox à la VBox principale
                    if (mosqueVBox != null) {
                        mosqueVBox.getChildren().add(mosqueHBox);
                    }
                }

                mosquesLocations.append("]");

                if (webView != null && webView.getEngine() != null) {
                    webView.getEngine().executeScript("updateMapWithMosques(" + mosquesLocations.toString() + ");");
                    System.out.println("Pharmacies envoyées à la carte.");
                } else {
                    System.out.println("WebView ou son moteur non initialisé.");
                }
            } else {
                System.out.println("Aucune pharmacie trouvée dans la zone.");
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
    private void handleLogout() {
        System.out.println("Le bouton Logout a été cliqué !");

        // Rediriger vers la page de connexion
        goToLoginPage();  // Cette méthode redirige vers la page de connexion
    }

    private void goToLoginPage() {
        // Rediriger vers la page de connexion
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();  // Obtenir la fenêtre actuelle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));  // Charger le fichier FXML de la page de connexion
        Parent root;
        try {
            root = loader.load();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);  // Appliquer la nouvelle scène (page de connexion)
            currentStage.show();
            System.out.println("Redirection vers la page de connexion.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la redirection vers la page de connexion.");
        }
    }

    @FXML
    private void handleCloseItinerary() {
        // Masquer le VBox
        itineraryFieldsVBox.setVisible(false);
        itineraryFieldsVBox.setManaged(false);

        // Masquer la barre latérale si elle existe (par exemple un Pane, VBox, ou autre conteneur)


        // Supprimer l'itinéraire de la carte (via le WebEngine de WebView)

    }




    public void handleRecentSearches(ActionEvent actionEvent) {
         // Cette méthode affiche les hôtels sur la carte
        hideAllVBoxes();  // Masque tous les VBox et ScrollPanes

        // Rendre visible le ScrollPane du restaurant (et donc son VBox)
        recherchrecenteScrollPane.setVisible(true);
        recherchrecenteScrollPane.setManaged(true);
        displayRecentSearches();

    }
    private void displayRecentSearches() {
        // Effacer les anciens éléments affichés
        recentSearchesVBox.getChildren().clear();

        // Récupérer le username de l'utilisateur connecté
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");
        int userId = getUserIdByUsername(username);

        if (userId != -1) {
            // Récupérer les recherches récentes
            List<Map<String, String>> recentSearches = getRecentSearches(userId);

            // Créer un ScrollPane pour ajouter du défilement
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);  // S'assurer que le contenu du ScrollPane s'adapte à la largeur
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Activer la barre de défilement horizontale si nécessaire
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Activer la barre de défilement verticale si nécessaire

            // Organiser le contenu dans un VBox
            VBox vbox = new VBox(10); // Espacement de 10px entre chaque élément

            // Ajouter les recherches dynamiquement au VBox
            for (Map<String, String> search : recentSearches) {
                String term = search.get("term");
                String date = search.get("date");

                // Créer une étiquette pour chaque recherche
                HBox searchHBox = new HBox(15); // Espacement de 15px entre le terme et la date
                searchHBox.setStyle("-fx-padding: 8px; -fx-background-color: #f9f9f9; -fx-border-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 3);");

                // Termes de recherche
                Label termLabel = new Label(term);
                termLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                // Date de recherche
                Label dateLabel = new Label("(" + date + ")");
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-style: italic;");

                // Ajouter les labels à l'HBox
                searchHBox.getChildren().addAll(termLabel, dateLabel);

                // Ajouter l'HBox au VBox
                vbox.getChildren().add(searchHBox);
            }

            // Ajouter le VBox au ScrollPane
            scrollPane.setContent(vbox);

            // Remplacer l'ancien VBox par le ScrollPane dans le conteneur principal
            recentSearchesVBox.getChildren().add(scrollPane);

        } else {
            System.out.println("Utilisateur non trouvé.");
        }
    }

    public List<Map<String, String>> getRecentSearches(int userId) {
        List<Map<String, String>> recentSearches = new ArrayList<>();
        String query = "SELECT NAME, MAX(SEARCHED_AT) AS SEARCHED_AT " +
                "FROM SEARCH_HISTORY " +
                "WHERE user_id = ? " +
                "GROUP BY NAME " +
                "ORDER BY SEARCHED_AT DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, String> search = new HashMap<>();
                search.put("term", resultSet.getString("NAME"));
                search.put("date", resultSet.getTimestamp("SEARCHED_AT").toString());
                recentSearches.add(search);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recentSearches;
    }


    public void handleadresseenredgistre(ActionEvent actionEvent) {
        // Cette méthode affiche les hôtels sur la carte
        hideAllVBoxes();  // Masque tous les VBox et ScrollPanes

        // Rendre visible le ScrollPane du restaurant (et donc son VBox)
        enregistreScrollPane.setVisible(true);
        enregistreScrollPane.setManaged(true);
        displayenregistres();

    }
    private void displayenregistres() {
        // Effacer les anciens éléments affichés
        enregistreVBox.getChildren().clear();

        // Récupérer le username de l'utilisateur connecté
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");
        int userId = getUserIdByUsername(username);

        if (userId != -1) {
            // Récupérer les recherches récentes
            List<Map<String, String>> enregistres = getenregistres(userId);

            // Créer un ScrollPane pour ajouter du défilement
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);  // S'assurer que le contenu du ScrollPane s'adapte à la largeur
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Activer la barre de défilement horizontale si nécessaire
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Activer la barre de défilement verticale si nécessaire

            // Organiser le contenu dans un VBox
            VBox vbox = new VBox(10); // Espacement de 10px entre chaque élément

            // Ajouter les recherches dynamiquement au VBox
            for (Map<String, String> search : enregistres) {
                String term = search.get("term");
                String date = search.get("date");

                // Créer une étiquette pour chaque recherche
                HBox enregistreHBox = new HBox(15); // Espacement de 15px entre le terme et la date
                enregistreHBox.setStyle("-fx-padding: 8px; -fx-background-color: #f9f9f9; -fx-border-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 3);");

                // Termes de recherche
                Label termLabel = new Label(term);
                termLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                // Date de recherche
                Label dateLabel = new Label("(" + date + ")");
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-style: italic;");

                // Ajouter les labels à l'HBox
                enregistreHBox.getChildren().addAll(termLabel, dateLabel);

                // Ajouter l'HBox au VBox
                vbox.getChildren().add(enregistreHBox);
            }

            // Ajouter le VBox au ScrollPane
            scrollPane.setContent(vbox);

            // Remplacer l'ancien VBox par le ScrollPane dans le conteneur principal
            enregistreVBox.getChildren().add(scrollPane);

        } else {
            System.out.println("Utilisateur non trouvé.");
        }
    }



    public List<Map<String, String>> getenregistres(int userId) {
        List<Map<String, String>> enregistres = new ArrayList<>();
        String query = "SELECT NAME,CREATED_AT " +
                "FROM lOCATIONS " +
                "WHERE user_id = ? " +
                "ORDER BY CREATED_AT DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, String> search = new HashMap<>();
                search.put("term", resultSet.getString("NAME"));
                search.put("date", resultSet.getTimestamp("CREATED_AT").toString());
                enregistres.add(search);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enregistres;
    }

    public void handleProfileDisplay(ActionEvent actionEvent) {
        // Masquer les autres éléments et afficher le ScrollPane du profil
        hideAllVBoxes();
        profileScrollPane.setVisible(true);
        profileScrollPane.setManaged(true);
        displayUserProfile();
    }

    private void displayUserProfile() {
        // Effacer les anciens éléments affichés
        profileVBox.getChildren().clear();

        // Créer un titre pour les informations personnelles
        Label titleLabel = new Label("Les Infos Personnelles");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10px;");
        profileVBox.getChildren().add(titleLabel);

        // Récupérer le username de l'utilisateur connecté
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");
        Map<String, String> userInfo = getUserInfoByUsername(username);

        if (!userInfo.isEmpty()) {
            // Afficher les informations de l'utilisateur
            Label userInfoLabel = new Label("Nom complet : " + userInfo.get("username") + "\n" +
                    "Email : " + userInfo.get("email"));
            userInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-padding: 10px;");
            userInfoLabel.setMaxWidth(400); // Limite la largeur maximale du label
            profileVBox.getChildren().add(userInfoLabel);

            // Ajouter un titre pour les fonctionnalités
            Label featuresTitle = new Label("Fonctionnalités Disponibles");
            featuresTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10px;");
            profileVBox.getChildren().add(featuresTitle);

            // Ajouter la liste des fonctionnalités
            List<String> features = getAppFeatures(); // Méthode pour obtenir les fonctionnalités
            for (String feature : features) {
                Text featureText = new Text("• " + feature);
                featureText.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-padding: 5px;");
                featureText.setWrappingWidth(400); // Limite la largeur pour éviter des débordements horizontaux
                profileVBox.getChildren().add(featureText);
            }
        } else {
            // Ajouter un message d'erreur si l'utilisateur n'est pas trouvé
            Label errorLabel = new Label("Utilisateur non trouvé.");
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red; -fx-padding: 10px;");
            profileVBox.getChildren().add(errorLabel);
        }

        // Afficher ou cacher le ScrollPane
        profileScrollPane.setVisible(true);
        profileScrollPane.setManaged(true);
    }

    private List<String> getAppFeatures() {
        // Liste des fonctionnalités disponibles (vous pouvez les rendre dynamiques)
        return Arrays.asList(
                "la rechreche et la detection des localisations ",
                "Afficher des lieux comme les pharmacies ,les hopitaux...",
                "La consultation des recherches recentes et les lieux visites  ",
                "rechercher des itineraires "





        );
    }

    public Map<String, String> getUserInfoByUsername(String username) {
        Map<String, String> userInfo = new HashMap<>();
        String query = "SELECT id, username, email FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                userInfo.put("id", String.valueOf(resultSet.getInt("id")));
                userInfo.put("username", resultSet.getString("username"));
                userInfo.put("email", resultSet.getString("email"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userInfo;
    }
    public String calculateRoute(double startLat, double startLon, double endLat, double endLon, String vehicle) throws IOException {
        String API_URL = "https://graphhopper.com/api/1/route";
        String apiKey= "0f5d7a61-cd15-4cb7-a179-8319fdf9912f";
        String url = String.format(Locale.ENGLISH,
                "%s?point=%f,%f&point=%f,%f&type=json&locale=en&vehicle=%s&key=%s",
                API_URL, startLat, startLon, endLat, endLon, vehicle, apiKey
        );
        System.out.println("URL API GraphHopper : " + url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            // Envoyer la requête et récupérer la réponse
            HttpEntity entity = client.execute(request).getEntity();
            String responseString = EntityUtils.toString(entity);

            System.out.println("Réponse complète de l'API : " + responseString);

            // Traiter la réponse JSON pour extraire les coordonnées
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseString);
            JsonNode pathNode = rootNode.path("paths");

            if (pathNode.isArray() && pathNode.size() > 0) {
                JsonNode path = pathNode.get(0);


                // Extraire le temps (en millisecondes)
                long travelTimeMillis = path.path("time").asLong();
                System.out.println("Durée estimée du trajet (millisecondes) : " + travelTimeMillis);

                // Convertir en minutes pour plus de lisibilité
                long travelTimeMinutes = travelTimeMillis / 60000;
                System.out.println("Durée estimée du trajet (minutes) : " + travelTimeMinutes);

                return decodePolylineForJS(path);
            } else {
                throw new RuntimeException("Aucune route trouvée.");
            }
        }
    }

    public String decodePolylineForJS(JsonNode path) {
        String encodedPoints = path.path("points").asText();
        /* System.out.println("Points encodés (Polyline) : " + encodedPoints);*/

        // Décoder la polyline
        List<GHPoint> decodedPoints = com.example.service.PolylineEncoder.decodePolyline(encodedPoints, 1e5);
        System.out.println("Nombre de points décodés : " + decodedPoints.size());

        // Créer la chaîne formatée pour JavaScript
        StringBuilder routeCoordinates = new StringBuilder("[");
        for (GHPoint point : decodedPoints) {
            // Inverser les coordonnées : latitude en premier
            routeCoordinates.append("[").append(point.lat).append(",").append(point.lon).append("],");
           /* System.out.println(String.format("Point décodé - Latitude : %f, Longitude : %f", point.lat, point.lon));*/
        }

        // Supprimer la dernière virgule
        if (routeCoordinates.length() > 1) {
            routeCoordinates.setLength(routeCoordinates.length() - 1);
        }
        routeCoordinates.append("]");

        /*  System.out.println("Coordonnées au format JavaScript : " + routeCoordinates);*/
        return routeCoordinates.toString();
    }
    // Variable pour stocker le mode de transport sélectionné
    private String selectedVehicle = "car"; // Par défaut, on commence avec le mode "car"
    @FXML
    private void handleCarMode() {
        selectedVehicle = "car";
        calculateAndDisplayItinerary();
    }

    @FXML
    private void handleBikeMode() {
        selectedVehicle = "bike";
        calculateAndDisplayItinerary();
    }

    @FXML
    private void handleWalkMode() {
        selectedVehicle = "foot";
        calculateAndDisplayItinerary();
    }
    private String formatTravelTime(long milliseconds) {
        long totalMinutes = milliseconds / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours >= 24) {
            long days = hours / 24;
            hours = hours % 24;
            return days + " jour" + (days > 1 ? "s" : "") + (hours > 0 ? " " + hours + "h" : "");
        } else if (hours > 0) {
            return hours + "h" + (minutes > 0 ? minutes : "");
        } else {
            return minutes + " min";
        }
    }


    private void calculateAndDisplayItinerary(){
        // Afficher les indicateurs de chargement
        loadingIndicator.setVisible(true);
        loadingLabel.setVisible(true);

        // Désactiver les champs
        String startCoordinates = startField.getText();
        String endCoordinates = endField.getText();

        if (startCoordinates.isEmpty() || endCoordinates.isEmpty()) {
            System.out.println("Veuillez remplir les deux champs.");
            loadingIndicator.setVisible(false);
            loadingLabel.setVisible(false);
            return;
        }
        try {
            double[] startCoords =getCoordinates(startCoordinates);
            double[] endCoords =getCoordinates(endCoordinates);
            System.out.println("Coordonnées de départ : " + startCoords[0] + ", " + startCoords[1]);
            System.out.println("Coordonnées d'arrivée : " + endCoords[0] + ", " + endCoords[1]);

            // Calculer les durées pour chaque mode
            long carTime = extractTravelTime(startCoords[0], startCoords[1], endCoords[0], endCoords[1], "car");
            long bikeTime = extractTravelTime(startCoords[0], startCoords[1], endCoords[0], endCoords[1], "bike");
            long walkTime = extractTravelTime(startCoords[0], startCoords[1], endCoords[0], endCoords[1], "foot");

            // Mettre à jour les labels
            carDurationLabel.setText(formatTravelTime(carTime));
            bikeDurationLabel.setText(formatTravelTime(bikeTime));
            walkDurationLabel.setText(formatTravelTime(walkTime));
            // Appeler pour récupérer les points de l'itinéraire
            String routeCoordinatesJS =calculateRoute(startCoords[0], startCoords[1], endCoords[0], endCoords[1], selectedVehicle);
            // Masquer les indicateurs de chargement
            loadingIndicator.setVisible(false);
            loadingLabel.setVisible(false);

            // Réactiver les champs
            startField.setDisable(false);
            endField.setDisable(false);

            // Afficher le contenu de routeCoordinatesJS dans la console
            System.out.println("Route Coordinates (JS format): " + routeCoordinatesJS);

            /* System.out.println("un teste ici" + routeCoordinatesJS);*/
            // Injecter les points dans OpenLayers via WebEngine


            webView.getEngine().executeScript("drawRouteOnMap(" + routeCoordinatesJS + ");");
            // Ajouter des marqueurs pour le départ et l'arrivée
            // Ajouter des marqueurs pour le départ et l'arrivée **après** avoir tracé l'itinéraire


        }catch (Exception e) {
            loadingIndicator.setVisible(false);
            loadingLabel.setVisible(false);
            System.out.println("Erreur : " + e.getMessage());
        }


    }

    private long extractTravelTime(double startLat, double startLon, double endLat, double endLon, String vehicle) throws IOException {
        String API_URL = "https://graphhopper.com/api/1/route";
        String apiKey = "0f5d7a61-cd15-4cb7-a179-8319fdf9912f";
        String url = String.format(Locale.ENGLISH,
                "%s?point=%f,%f&point=%f,%f&type=json&locale=en&vehicle=%s&key=%s",
                API_URL, startLat, startLon, endLat, endLon, vehicle, apiKey
        );

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpEntity entity = client.execute(request).getEntity();
            String responseString = EntityUtils.toString(entity);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseString);
            JsonNode pathNode = rootNode.path("paths");

            if (pathNode.isArray() && pathNode.size() > 0) {
                JsonNode path = pathNode.get(0);
                return path.path("time").asLong(); // Retourner le temps en millisecondes
            } else {
                throw new RuntimeException("Aucune route trouvée.");
            }
        }}

    private double[] getCoordinates(String query) {
        try {
            // Encoder la requête
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            // URL pour la requête Nominatim
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&addressdetails=1&limit=1";

            // Envoi de la requête
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    // Vérifier le statut de la réponse
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JSONArray results = new JSONArray(jsonResponse);

                        // Vérifier si des résultats sont disponibles
                        if (results.length() > 0) {
                            JSONObject location = results.getJSONObject(0);
                            double latitude = location.optDouble("lat");
                            double longitude = location.optDouble("lon");

                            // Retourner les coordonnées
                            return new double[]{latitude, longitude};
                        } else {
                            System.out.println("Aucun lieu trouvé pour : " + query);


                        }
                    } else {
                        System.err.println("Erreur lors de la requête HTTP : " + response.getStatusLine());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la recherche de la localisation : " + e.getMessage());
        }
        return null; // Retourner null si les coordonnées ne sont pas disponibles
    }


}






