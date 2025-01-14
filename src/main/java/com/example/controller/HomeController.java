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
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import com.sun.net.httpserver.HttpServer;
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
import javafx.scene.layout.VBox;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;
import javafx.geometry.Insets;

public class HomeController {

    public Label errorLabel;
    public Label errorlabel;
    public VBox meteoVBox;
    public VBox meteoafficherVBox;

    private HttpServer server;
    @FXML
    private TextField locationField;
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
    public Button closeButton;
   public Button meteooButton ;
    public ImageView closIcon;
    public ProgressIndicator loadingIndicator;
    public Label walkDurationLabel;
    public Button walkModeButton;
    public Button bikeModeButton;
    public Button carModeButton;
    public Label carDurationLabel;
    public Label bikeDurationLabel;
    public Button closeButtonpro;
    public Button detectLocationButton;
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


    private WebEngine webEngine;
    private ObjectStreamClass mainAnchorPane;
    private String coordinates;
    private String searchQuery;

    public HomeController() {

        this.geolocationService = new GeolocationService();
        try {
            startHttpServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Déclaration des champs pour la position de départ et d'arrivée
    @FXML
    private VBox itineraryFieldsVBox;

    @FXML
    private TextField startField;

    @FXML
    private TextField endField;


    private double[] selectedEndCoordinates;
    private double[] selectedStartCoordinates;
    private boolean isStartFieldActive = false;

    private boolean issearchFieldActive = false;
    private boolean isEndFieldActive = false;

    // Méthode qui sera appelée lorsque l'utilisateur clique sur le bouton "Itinéraire"
    public  static String getLocationNameFromCoordinate(double latitude, double longitude) {
        String nominatimUrl = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude;

        try {
            URL url = new URL(nominatimUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse la réponse JSON pour récupérer le "display_name"
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.optString("display_name", "Nom inconnu");
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la récupération du nom";
        }
    }

    public void startHttpServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/coordinates", new CoordinatesHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Serveur démarré sur http://localhost:8000");

        // Ajouter un shutdown hook pour fermer le serveur à la fermeture de l'application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopHttpServer();
        }));
    }




    class CoordinatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Lire les données reçues
                InputStream requestBody = exchange.getRequestBody();
                String requestData = new BufferedReader(new InputStreamReader(requestBody))
                        .lines()
                        .collect(Collectors.joining("\n"));

                System.out.println("Données reçues depuis JavaScript : " + requestData);

                try {
                    // Parser les coordonnées reçues (attendues au format JSON)
                    JSONArray requestArray = new JSONArray(requestData); // Parser le tableau JSON
                    JSONObject requestJson = requestArray.getJSONObject(0); // Récupérer le premier objet du tableau

                    double latitude = requestJson.getDouble("lat");
                    double longitude = requestJson.getDouble("lng");
                    String markerName = requestJson.optString("name", "Nom inconnu");

                    System.out.println("Latitude : " + latitude);
                    System.out.println("Longitude : " + longitude);
                    System.out.println("Nom : " + markerName);

                    // Récupérer le nom de l'endroit via un service de géocodage inverse
                   final String locationName = getLocationNameFromCoordinate(latitude, longitude);
                    System.out.println("Nom de la localisation : " + locationName);


                    // Mettre à jour les champs correspondants


                        Platform.runLater(() -> {
                            if (locationName == null || locationName.isEmpty()) {
                                // Afficher un message d'erreur si aucun nom n'est trouvé
                                errorLabel.setText("Erreur : emplacement inconnu.");
                                errorLabel.setVisible(true);
                                errorLabel.setManaged(true);
                            }
                                if (issearchFieldActive) {
                                    searchField.setText(locationName);
                                    System.out.println("searchField mis à jour avec : " + locationName);
                                } else if (isStartFieldActive) {
                                    startField.setText(locationName);
                                    System.out.println("startField mis à jour avec : " + locationName);
                                } else if (isEndFieldActive) {
                                    endField.setText(locationName);
                                    System.out.println("endField mis à jour avec : " + locationName);
                                } else {
                                    System.out.println("Aucun champ actif trouvé !");
                                }

                                // Réinitialiser les indicateurs
                                issearchFieldActive = false;
                                isStartFieldActive = false;
                                isEndFieldActive = false;

                                System.out.println("Indicateurs réinitialisés.");
                            });

                            // Envoyer une réponse simple
                    // Créer la réponse JSON
                    String response = "{\"status\":\"success\", \"message\":\"Coordonnées reçues\"}";

// Convertir la réponse en tableau d'octets (UTF-8)
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

// Envoyer les en-têtes avec la longueur correcte
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, responseBytes.length);

// Envoyer le corps de la réponse
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();

                } catch (Exception e) {
                    System.err.println("Erreur lors du traitement des données reçues : " + e.getMessage());
                    e.printStackTrace();

                    String response = "{\"status\":\"error\", \"message\":\"Erreur de traitement des données\"}";
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(400, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    public void stopHttpServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Serveur arrêté.");
        }
    }



    @FXML
    private void hideAllVBoxes() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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

        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        meteoVBox.setVisible(false);
        meteoVBox.setManaged(false);
        meteoafficherVBox.setVisible(false);
        meteoafficherVBox.setManaged(false);

    }
    private void resetItineraryFields() {
        // Effacer les champs de départ et d'arrivée
        startField.clear();
        endField.clear();

        // Réinitialiser les labels de durée
        carDurationLabel.setText(" ");
        bikeDurationLabel.setText(" ");
        walkDurationLabel.setText(" ");

        // Réinitialiser la sélection du mode de transport
        selectedVehicle = null;

        // Cacher les indicateurs de chargement si visibles

    }
    @FXML
    private void handleItinerary() {
        // Cacher toutes les autres VBoxes
        hideAllVBoxes();
        resetItineraryFields();

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

                            // Extraire les différents éléments d'adresse
                            String city = json.optJSONObject("address").optString("city", null);
                            String county = json.optJSONObject("address").optString("county", null);
                            String country = json.optJSONObject("address").optString("country", null);
                            String displayName = json.optString("display_name", "Nom de la localisation introuvable");

                            if (city == null) {
                                // Si la ville n'est pas trouvée, vérifier le comté
                                if (county != null) {
                                    System.out.println("Comté : " + county);

                                    // Diviser le 'county' par des espaces et extraire la troisième chaîne
                                    String[] countyParts = county.split(" "); // Séparer par les espaces
                                    if (countyParts.length >= 3) {
                                        String thirdPartOfCounty = countyParts[2].trim();  // Extraire la troisième partie
                                        System.out.println("Troisième partie du comté : " + thirdPartOfCounty);
                                        // Recherche avec la troisième partie du comté
                                        getWikipediaDetails(thirdPartOfCounty);
                                    } else {
                                        // Si la troisième partie n'existe pas, on peut faire une recherche avec l'intégralité du comté
                                        getWikipediaDetails(county);  // Recherche avec l'intégralité du comté
                                    }
                                } else if (country != null) {
                                    System.out.println("Pays : " + country);

                                    // Diviser le 'country' par des espaces et extraire la troisième chaîne
                                    String[] countryParts = country.split(" "); // Séparer par les espaces
                                    if (countryParts.length >= 3) {
                                        String thirdPartOfCountry = countryParts[2].trim();  // Extraire la troisième partie
                                        System.out.println("Troisième partie du pays : " + thirdPartOfCountry);
                                        // Recherche avec la troisième partie du pays
                                        getWikipediaDetails(thirdPartOfCountry);
                                    } else {
                                        // Si la troisième partie n'existe pas, on peut faire une recherche avec l'intégralité du pays
                                        getWikipediaDetails(country);  // Recherche avec l'intégralité du pays
                                    }
                                } else {
                                    // Si aucun élément n'est trouvé, utiliser "display_name"
                                    System.out.println("Localisation complète : " + displayName);
                                    getWikipediaDetails(displayName);  // Recherche avec l'adresse complète
                                }
                            } else {
                                System.out.println("Ville : " + city);
                                getWikipediaDetails(city);  // Recherche avec la ville
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
            try {
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
                    throw new RuntimeException("Le fichier HTML de la carte est introuvable !");
                }

                // Ajouter un écouteur pour vérifier quand la carte est prête
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        System.out.println("Carte prête pour les interactions.");
                        webEngine.executeScript("alert('Carte chargée');"); // Test pour confirmer le chargement
                    } else if (newState == Worker.State.FAILED) {
                        System.err.println("Erreur lors du chargement de la carte.");
                        chargerVueDeSecours(webEngine);
                    }
                });
                searchField.setOnMouseClicked(event -> {
                    issearchFieldActive = true;
                    isStartFieldActive = false;
                    isEndFieldActive = false;
                    System.out.println("searchField activé.");
                });

                startField.setOnMouseClicked(event -> {
                    issearchFieldActive = false;
                    isStartFieldActive = true;
                    isEndFieldActive = false;
                    System.out.println("startField activé.");
                });

                endField.setOnMouseClicked(event -> {
                    issearchFieldActive = false;
                    isStartFieldActive = false;
                    isEndFieldActive = true;
                    System.out.println("endField activé.");
                });

                // Gestion des erreurs JavaScript
                webEngine.setOnError(event -> System.out.println("Erreur JavaScript : " + event.getMessage()));
                webEngine.setOnAlert(event -> System.out.println(event.getData())); // pour capturer les alertes JavaScript

                System.out.println("Carte chargée : " + webEngine.getLoadWorker().getState());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation de la carte : " + e.getMessage());
                chargerVueDeSecours(webView.getEngine());
            }
        }
    }
    // Méthode pour exposer Java à JavaScript
    public void setupJavaScriptBridge() {
        WebEngine webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaController", this); // Expose l'instance actuelle du contrôleur
            }
        });
    }

    // Méthode appelée depuis JavaScript
    public void receiveJson(String jsonResponse) {
        System.out.println("Données reçues de JavaScript : " + jsonResponse);
    }


    private void chargerVueDeSecours(WebEngine webEngine) {
        try {
            System.out.println("Chargement de la vue de secours...");
            String backupFilePath = getClass().getResource("/html/backup.html").toExternalForm();

            if (backupFilePath != null) {
                webEngine.load(backupFilePath);
                System.out.println("Vue de secours chargée avec succès.");
            } else {
                System.err.println("Le fichier de secours est introuvable !");
                webEngine.loadContent("<html><body><h1>Erreur</h1><p>La carte n'a pas pu être chargée. Veuillez réessayer plus tard.</p></body></html>");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la vue de secours : " + e.getMessage());
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
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];" +
                "(node(around:20000," + longitude + "," + latitude + ")[amenity=taxi];" +
                "node(around:20000," + latitude + "," + longitude + ")[railway=tram_stop];" +
                "node(around:20000," + latitude + "," + longitude + ")[highway=bus_stop];);out;";

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

            if (transportVBox != null) {
                transportVBox.getChildren().clear();
            }

            if (elements.length() > 0) {
                System.out.println("Transports trouvés :");

                StringBuilder transportsLocations = new StringBuilder("[");

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
                    transportLabel.setStyle(
                            "-fx-font-size: 14px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-text-fill: #333; " +

                                    "-fx-padding: 8px; " +

                                    "-fx-border-radius: 4px;"
                    );
                    transportHBox.setStyle(
                            "-fx-padding: 10; " +

                                    "-fx-background-color: F1F6F9; " +
                                    "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.2), 5, 0, 2, 2);"
                    );

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Bouton "Enregistrer"
                    Button saveButton = new Button();
                    Image saveImage = new Image(getClass().getResource("/images/save-icon.png").toString());
                    ImageView saveImageView = new ImageView(saveImage);
                    saveImageView.setFitWidth(20);
                    saveImageView.setFitHeight(20);
                    saveButton.setGraphic(saveImageView);
                    saveButton.setStyle(
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; "+
                                    "-fx-padding: 7; "+
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        storeLocationInDatabase(name);
                    });

                    // Bouton "Itinéraire"
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; "+
                                    "-fx-padding: 7; "+
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        // Logique de calcul d'itinéraire ici
                        hideAllVBoxes();
                        resetItineraryFields(); // Réinitialise la VBox
                        itineraryFieldsVBox.setVisible(true); // Affiche la VBox d'itinéraire
                        itineraryFieldsVBox.setManaged(true);

                        // Remplir le champ de destination avec le nom de la mosquée sélectionnée
                        endField.setText(name);

                        double[] selectedEndCoordinates = new double[]{transport.optDouble("lat"), transport.optDouble("lon")};

                        // Effacer le champ de départ (à remplir par l'utilisateur)
                        startField.clear();
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
                // Aucun transport trouvé
                System.out.println("Aucun transport trouvé dans la zone.");
                if (transportVBox != null) {
                    transportVBox.getChildren().clear();
                    Label noTransportLabel = new Label("Aucun transport trouvé dans cette zone.");
                    noTransportLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");
                    transportVBox.getChildren().add(noTransportLabel);
                }
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
                System.out.println("Hôtels trouvés :");

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
                    hotelHBox.setAlignment(Pos.CENTER_LEFT);
                    hotelHBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

                    Label hotelLabel = new Label(name + "\n" + hotelInfo.toString());
                    hotelLabel.setWrapText(true);
                    hotelLabel.setStyle(
                            "-fx-font-size: 14px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-text-fill: #333; " +
                                    "-fx-padding: 8px; " +
                                    "-fx-border-radius: 4px;"
                    );
                    hotelHBox.setStyle(
                            "-fx-padding: 10; " +
                                    "-fx-background-color: F1F6F9; " +
                                    "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.2), 5, 0, 2, 2);"
                    );

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Bouton "Enregistrer"
                    Button saveButton = new Button();
                    Image saveImage = new Image(getClass().getResource("/images/save-icon.png").toString());
                    ImageView saveImageView = new ImageView(saveImage);
                    saveImageView.setFitWidth(20);
                    saveImageView.setFitHeight(20);
                    saveButton.setGraphic(saveImageView);
                    saveButton.setStyle(
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; " +
                                    "-fx-padding: 7; " +
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        storeLocationInDatabase(name);
                    });

                    // Bouton "Itinéraire"
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; " +
                                    "-fx-padding: 7; " +
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        // Logique de calcul d'itinéraire ici
                        hideAllVBoxes();
                        resetItineraryFields(); // Réinitialise la VBox
                        itineraryFieldsVBox.setVisible(true); // Affiche la VBox d'itinéraire
                        itineraryFieldsVBox.setManaged(true);

                        // Remplir le champ de destination avec le nom de l'hôtel sélectionné
                        endField.setText(name);

                        double[] selectedEndCoordinates = new double[]{hotel.optDouble("lat"), hotel.optDouble("lon")};

                        // Effacer le champ de départ (à remplir par l'utilisateur)
                        startField.clear();
                    });

                    VBox buttonsBox = new VBox(10, saveButton, routeButton);
                    buttonsBox.setAlignment(Pos.TOP_RIGHT);

                    hotelHBox.getChildren().addAll(hotelLabel, spacer, buttonsBox);

                    if (restaurantVBox != null) {
                        restaurantVBox.getChildren().add(hotelHBox);
                    }
                }

                hotelsLocations.append("]");

                if (webView != null && webView.getEngine() != null) {
                    webView.getEngine().executeScript("updateMapWithRestaurants(" + hotelsLocations.toString() + ");");
                    System.out.println("Hôtels envoyés à la carte.");
                } else {
                    System.out.println("WebView ou son moteur non initialisé.");
                }
            }  else {
                hideAllVBoxes();
                restaurantVBox.setManaged(true);
                restaurantVBox.setVisible(true);
                System.out.println("Aucun hotel trouvé dans la zone.");
                if (restaurantVBox != null) {
                    restaurantVBox.getChildren().clear();
                    Label noTransportLabel = new Label("Aucun thotel trouvé dans cette zone.");
                    noTransportLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");
                    restaurantVBox.getChildren().add(noTransportLabel);
                }
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
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:2000," + longitude + "," + latitude + ")[amenity=pharmacy];out;";

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
                    pharmacyHBox.setStyle(
                            "-fx-padding: 10; " +
                                    "-fx-background-color: F1F6F9; " +
                                    "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.2), 5, 0, 2, 2);"
                    );

                    // Ajoutez toutes les informations sous forme de texte
                    Text pharmacyText = new Text("Nom : " + name + "\nInformations : \n" + pharmacyInfo.toString());
                    pharmacyText.setStyle( "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #333; " +
                            "-fx-padding: 8px; " +
                            "-fx-border-radius: 4px;");
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
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; "+
                                    "-fx-padding: 7; "+
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        storeLocationInDatabase(name);
                    });

                    // Bouton "Itinéraire"
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; "+
                                    "-fx-padding: 7; "+
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        hideAllVBoxes();
                        resetItineraryFields(); // Réinitialise la VBox
                        itineraryFieldsVBox.setVisible(true); // Affiche la VBox d'itinéraire
                        itineraryFieldsVBox.setManaged(true);

                        // Remplir le champ de destination avec le nom de la mosquée sélectionnée
                        endField.setText(name);
                        // Remplir le champ de destination avec un format précis
                        double[] selectedEndCoordinates = new double[]{pharmacy.optDouble("lat"), pharmacy.optDouble("lon")};

                        // Effacer le champ de départ (à remplir par l'utilisateur)
                        startField.clear();
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
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:9000," + longitude + "," +latitude + ")[amenity=place_of_worship];out;";
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
                    mosqueHBox.setStyle(
                            "-fx-padding: 10; " +
                                    "-fx-background-color: F1F6F9; " +
                                    "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.2), 5, 0, 2, 2);"
                    );
                    mosqueText.setWrappingWidth(400); // Limitez la largeur pour éviter des débordements
                    mosqueText.setStyle( "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #333; " +
                            "-fx-padding: 8px; " +
                            "-fx-border-radius: 4px;");
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
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; "+
                                    "-fx-padding: 7; "+
                                    "-fx-cursor: hand;"
                    );
                    saveButton.setOnAction(event -> {
                        System.out.println("Bouton Enregistrer cliqué pour : " + name);
                        storeLocationInDatabase(name);

                    });

                    // Bouton "Itinéraire"
                    Button routeButton = new Button();
                    Image routeImage = new Image(getClass().getResource("/images/itinerary-icon.png").toString());
                    ImageView routeImageView = new ImageView(routeImage);
                    routeImageView.setFitWidth(20);
                    routeImageView.setFitHeight(20);
                    routeButton.setGraphic(routeImageView);
                    routeButton.setStyle(
                            "-fx-border-color: #0078d7;" +
                                    "-fx-background-color: white; " +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 25;  " +
                                    "-fx-background-radius: 25; "+
                                    "-fx-padding: 7; "+
                                    "-fx-cursor: hand;"
                    );
                    routeButton.setOnAction(event -> {
                        System.out.println("Bouton Itinéraire cliqué pour : " + name);
                        hideAllVBoxes();
                        resetItineraryFields(); // Réinitialise la VBox
                        itineraryFieldsVBox.setVisible(true); // Affiche la VBox d'itinéraire
                        itineraryFieldsVBox.setManaged(true);

                        // Remplir le champ de destination avec le nom de la mosquée sélectionnée
                        endField.setText(name);

                        // Effacer le champ de départ (à remplir par l'utilisateur)
                        startField.clear();

                        // Sauvegarder les coordonnées exactes pour l'itinéraire
                        double[] selectedEndCoordinates = new double[]{mosque.optDouble("lat"), mosque.optDouble("lon")};

                        // Effacer le champ de départ (à remplir par l'utilisateur)
                        startField.clear();
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
         searchQuery = searchField.getText().trim();
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
                                // Stocker les coordonnées de la première localisation trouvée
                                if (i == 0) {
                                    coordinates = latitude + "," + longitude;
                                }
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
        searchField.clear();
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
    private Label statusLabel;

    @FXML
    private void handleSave() {
        String name = locationNameLabel.getText();


        storeLocationInDatabase(name);
        statusLabel.setText("Enregistrement réussi !");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);

        // Optionnel : Afficher un log dans la console pour vérification
        System.out.println("Bouton enregistré cliqué.");
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
    public void handleRecentSearches(ActionEvent actionEvent) {
        // Cette méthode affiche les hôtels sur la carte
        hideAllVBoxes();  // Masque tous les VBox et ScrollPanes

        // Rendre visible le ScrollPane du restaurant (et donc son VBox)
        recherchrecenteScrollPane.setVisible(true);
        recherchrecenteScrollPane.setManaged(true);
        displayRecentSearches();

    }

    private void displayRecentSearches() {
        // Effacer les anciens éléments affichés dans le VBox existant
        recentSearchesVBox.getChildren().clear();

        // Récupérer le username de l'utilisateur connecté
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");
        int userId = getUserIdByUsername(username);

        if (userId != -1) {
            // Récupérer les recherches récentes
            List<Map<String, String>> enregistres = getRecentSearches(userId);

            // Conteneur HBox pour le titre et le bouton de fermeture
            HBox headerBox = new HBox(15);
            headerBox.setStyle("-fx-alignment: center-left; -fx-padding: 10px; -fx-background-color: #e0f2f1;");

            // Titre
            Label titleLabel = new Label("Recherches Récentes");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #004d40;");

            // Espace dynamique entre le titre et le bouton
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Bouton avec une icône
            Button closeButton = new Button();
            Image closeImage = new Image(getClass().getResource("/images/close-icon.png").toString());
            ImageView closeImageView = new ImageView(closeImage);
            closeImageView.setFitWidth(20);
            closeImageView.setFitHeight(20);
            closeButton.setGraphic(closeImageView);
            closeButton.setStyle(
                    "-fx-border-color: #0078d7;" +
                            "-fx-background-color: white; " +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 25;  " +
                            "-fx-background-radius: 25; "+
                            "-fx-padding: 7; "+
                            "-fx-cursor: hand;"
            );
            closeButton.setOnAction(event -> {

                hideAllVBoxes();
                resetItineraryFields();
                itineraryFieldsVBox.setVisible(true);
                itineraryFieldsVBox.setManaged(true);




            });


            headerBox.getChildren().addAll(titleLabel, spacer, closeButton);
            recentSearchesVBox.getChildren().add(headerBox);

            // Boucle pour ajouter les données dynamiques
            for (Map<String, String> search : enregistres) {
                String term = search.get("term");
                String date = search.get("date");

                String truncatedDate = date.length() > 16 ? date.substring(0, 16) : date;

                HBox enregistreHBox = new HBox(10);
                enregistreHBox.setStyle(
                        "-fx-padding: 10px; " +
                                "-fx-background-color: #ffffff; " +
                                "-fx-border-color: #b2dfdb; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 8;"
                );

                Label termLabel = new Label(term);
                termLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #004d40; -fx-font-weight: bold;");

                Label dateLabel = new Label("(" + truncatedDate + ")");
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #616161; -fx-font-style: italic;");

                enregistreHBox.getChildren().addAll(termLabel, dateLabel);

                recentSearchesVBox.getChildren().add(enregistreHBox);
            }

            recentSearchesVBox.setPrefWidth(600);
            recentSearchesVBox.setMaxWidth(Double.MAX_VALUE);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(recentSearchesVBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

            recherchrecenteScrollPane.setContent(scrollPane);
            recherchrecenteScrollPane.setVisible(true);
            recherchrecenteScrollPane.setManaged(true);
        } else {
            Label errorLabel = new Label("Utilisateur non trouvé.");
            errorLabel.setStyle(
                    "-fx-font-size: 14px; -fx-text-fill: red; -fx-padding: 10px; -fx-font-weight: bold; -fx-background-color: #ffcdd2; -fx-border-radius: 5px; -fx-border-color: #b71c1c;"
            );
            recentSearchesVBox.getChildren().add(errorLabel);

            recherchrecenteScrollPane.setVisible(false);
            recherchrecenteScrollPane.setManaged(false);
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
        resetItineraryFields();
        enregistreScrollPane.setVisible(true);
        enregistreScrollPane.setManaged(true);
        displayenregistres();

    }
    @FXML
    private void handleClose() {
        System.out.println("Close button clicked");
        resetItineraryFields();
        enregistreScrollPane.setVisible(false);
        enregistreScrollPane.setManaged(false);
    }
    private void displayenregistres() {
        // Effacer les anciens éléments affichés dans le VBox existant
        enregistreVBox.getChildren().clear();

        // Récupérer le username de l'utilisateur connecté
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");
        int userId = getUserIdByUsername(username);

        if (userId != -1) {
            // Récupérer les recherches récentes
            List<Map<String, String>> enregistres = getenregistres(userId);

            // Conteneur HBox pour le titre et le bouton de fermeture
            HBox headerBox = new HBox(15);
            headerBox.setStyle("-fx-alignment: center-left; -fx-padding: 10px; -fx-background-color: #e0f2f1;");

            // Titre
            Label titleLabel = new Label("Lieux Visités");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #004d40;");

            // Espace dynamique entre le titre et le bouton
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Bouton avec une icône
            Button closeButton = new Button();
            Image closeImage = new Image(getClass().getResource("/images/close-icon.png").toString());
            ImageView closeImageView = new ImageView(closeImage);
            closeImageView.setFitWidth(20);
            closeImageView.setFitHeight(20);
            closeButton.setGraphic(closeImageView);
            closeButton.setStyle(
                    "-fx-border-color: #0078d7;" +
                            "-fx-background-color: white; " +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 25;  " +
                            "-fx-background-radius: 25; "+
                            "-fx-padding: 7; "+
                            "-fx-cursor: hand;"
            );
            closeButton.setOnAction(event -> {

                hideAllVBoxes();
                 resetItineraryFields();
                itineraryFieldsVBox.setVisible(true);
                itineraryFieldsVBox.setManaged(true);




            });


            headerBox.getChildren().addAll(titleLabel, spacer, closeButton);
            enregistreVBox.getChildren().add(headerBox);

            for (Map<String, String> search : enregistres) {
                String term = search.get("term");
                String date = search.get("date");

                String truncatedDate = date.length() > 16 ? date.substring(0, 16) : date;

                HBox enregistreHBox = new HBox(10);
                enregistreHBox.setStyle(
                        "-fx-padding: 10px; " +
                                "-fx-background-color: #ffffff; " +
                                "-fx-border-color: #b2dfdb; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 8;"
                );

                Label termLabel = new Label(term);
                termLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #004d40; -fx-font-weight: bold;");

                Label dateLabel = new Label("(" + truncatedDate + ")");
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #616161; -fx-font-style: italic;");

                enregistreHBox.getChildren().addAll(termLabel, dateLabel);

                enregistreVBox.getChildren().add(enregistreHBox);
            }

            enregistreVBox.setPrefWidth(600);
            enregistreVBox.setMaxWidth(Double.MAX_VALUE);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(enregistreVBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

            enregistreScrollPane.setContent(scrollPane);
            enregistreScrollPane.setVisible(true);
            enregistreScrollPane.setManaged(true);
        } else {
            Label errorLabel = new Label("Utilisateur non trouvé.");
            errorLabel.setStyle(
                    "-fx-font-size: 14px; -fx-text-fill: red; -fx-padding: 10px; -fx-font-weight: bold; -fx-background-color: #ffcdd2; -fx-border-radius: 5px; -fx-border-color: #b71c1c;"
            );
            enregistreVBox.getChildren().add(errorLabel);

            enregistreScrollPane.setVisible(false);
            enregistreScrollPane.setManaged(false);
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

        // Créer un conteneur HBox pour le titre et le bouton de fermeture
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-left; -fx-padding: 15px; -fx-background-color: #f0f4f8; -fx-border-color: #ccc; -fx-border-width: 1px;");

        // Ajouter un titre "Infos Personnelles"
        Label titleLabel = new Label("Les Infos Personnelles");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerBox.getChildren().add(titleLabel);

        // Ajouter un espace entre le titre et le bouton
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().add(spacer);

        // Créer le bouton de fermeture avec une icône
        Button closeButton = new Button();
        Image closeImage = new Image(getClass().getResource("/images/close-icon.png").toString());
        ImageView closeImageView = new ImageView(closeImage);
        closeImageView.setFitWidth(20);
        closeImageView.setFitHeight(20);

        closeButton.setGraphic(closeImageView);
        closeButton.setStyle(
                "-fx-border-color: #0078d7;" +
                        "-fx-background-color: white; " +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 25;  " +
                        "-fx-background-radius: 25; "+
                        "-fx-padding: 7; "+
                        "-fx-cursor: hand;"
        );

        // Action du bouton de fermeture
        closeButton.setOnAction(event -> {
            profileScrollPane.setVisible(false);
            profileScrollPane.setManaged(false);
resetItineraryFields();
            itineraryFieldsVBox.setVisible(true);
            itineraryFieldsVBox.setManaged(true);
        });

        headerBox.getChildren().add(closeButton);

        // Ajouter la HBox au VBox principal
        profileVBox.getChildren().add(headerBox);

        // Récupérer le username de l'utilisateur connecté
        String username = welcomeLabel.getText().replace("Bienvenue, ", "").replace(" !", "");
        Map<String, String> userInfo = getUserInfoByUsername(username);

        if (!userInfo.isEmpty()) {
            // Afficher les informations de l'utilisateur
            Label userInfoLabel = new Label("Nom complet : " + userInfo.get("username") + "\n" +
                    "Email : " + userInfo.get("email"));
            userInfoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555; -fx-padding: 15px; -fx-background-color: #e9ecef; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px;");
            userInfoLabel.setMaxWidth(400);
            profileVBox.getChildren().add(userInfoLabel);

            // Ajouter un titre pour les fonctionnalités
            Label featuresTitle = new Label("Fonctionnalités Disponibles");
            featuresTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 10px;");
            profileVBox.getChildren().add(featuresTitle);

            // Ajouter la liste des fonctionnalités avec un style amélioré
            List<String> features = getAppFeatures();
            for (String feature : features) {
                Text featureText = new Text("• " + feature);

                // Appliquer un style amélioré directement
                featureText.setStyle(
                        "-fx-font-size: 14px; " +
                                "-fx-text-fill: #2A9D8F; " +
                                "-fx-padding: 8px 12px; " +
                                "-fx-background-color: #E0FBFC; " +
                                "-fx-background-radius: 5px; " +
                                "-fx-border-color: #2A9D8F; " +
                                "-fx-border-width: 1px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 4, 0.5, 2, 2); " // Effet d'ombre
                );

                // Ajout au conteneur
                profileVBox.getChildren().add(featureText);
            }

        } else {
            // Ajouter un message d'erreur si l'utilisateur n'est pas trouvé
            Label errorLabel = new Label("Utilisateur non trouvé.");
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #E74C3C; -fx-padding: 10px; -fx-background-color: #f8d7da; -fx-border-color: #E74C3C; -fx-border-width: 1px; -fx-border-radius: 5px;");
            profileVBox.getChildren().add(errorLabel);
        }

        // Afficher ou cacher le ScrollPane
        profileScrollPane.setVisible(true);
        profileScrollPane.setManaged(true);
    }
    private List<String> getAppFeatures() {
        // Liste des fonctionnalités avancées pour votre application
        return Arrays.asList(
                "La recherche et la détection des localisations",
                "Afficher des lieux comme les pharmacies, les hôpitaux...",
                "La consultation des recherches récentes et les lieux visités",
                "Rechercher des itinéraires",
                "Planification d'itinéraires : Permettre à l'utilisateur de calculer l'itinéraire optimal entre deux adresses",
                "Offrir le choix du mode de transport : À pied, À vélo, En voiture",
                "Fournir une estimation du temps de trajet pour chaque mode de transport",
                "Afficher les itinéraires sur une carte interactive avec des indications claires des points de départ et d'arrivée",
                "Inclure des points d'intérêt tels que : Pharmacies, Hôtels, Mosquées, Moyens de transport",
                "Afficher les informations détaillées sur ces points d'intérêt (adresse, site web, etc.)",
                "Proposer la possibilité d'afficher l'itinéraire vers un point d'intérêt sélectionné",
                "Localisation de l'utilisateur : Afficher la position actuelle de l'utilisateur pour faciliter son orientation en temps réel"
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

        String startCoordinates = startField.getText();
        String endCoordinates = endField.getText();

        if (startCoordinates.isEmpty() || endCoordinates.isEmpty()) {
            System.out.println("Veuillez remplir les deux champs.");
            errorLabel.setText("Veuillez remplir les deux champs.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;}
        try {
            double[] startCoords =selectedStartCoordinates != null ? selectedStartCoordinates : getCoordinates(startCoordinates);
            double[] endCoords = selectedEndCoordinates != null ? selectedEndCoordinates : getCoordinates(endCoordinates);
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

            if (routeCoordinatesJS == null || routeCoordinatesJS.isEmpty()) {
                throw new Exception("Aucun itinéraire trouvé.");
            }
            // Réactiver les champs
            startField.setDisable(false);
            endField.setDisable(false);

            // Afficher le contenu de routeCoordinatesJS dans la console
           // System.out.println("Route Coordinates (JS format): " + routeCoordinatesJS);

            /* System.out.println("un teste ici" + routeCoordinatesJS);*/
            // Injecter les points dans OpenLayers via WebEngine
            webView.getEngine().executeScript("drawRouteOnMap(" + routeCoordinatesJS + ");");
            // Ajouter des marqueurs pour le départ et l'arrivée
            // Ajouter des marqueurs pour le départ et l'arrivée après avoir tracé l'itinéraire
// Masquer le label d'erreur si le calcul a réussi
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

        }catch (Exception e) {//
            System.out.println("Erreur : " + e.getMessage());
// Afficher un message d'erreur dans le label
            errorLabel.setText("Une erreur s'est produite : Aucun itinéraire trouvé.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            // Réactiver les champs même en cas d'erreur
            startField.setDisable(false);
            endField.setDisable(false);
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



    public void handleitinerary(ActionEvent actionEvent) {

        try {
            hideAllVBoxes();
            resetItineraryFields(); // Réinitialise la VBox
            itineraryFieldsVBox.setVisible(true); // Affiche la VBox d'itinéraire
            itineraryFieldsVBox.setManaged(true);

            // Récupérer les coordonnées de la localisation actuelle
            double[] selectedStartCoordinates = geolocationService.getLocationCoordinates();
            double latitude = selectedStartCoordinates[1];
            double longitude = selectedStartCoordinates[0];

            // Obtenir le nom de la position à partir des coordonnées
            String locationName = getLocationNameFromCoordinate(latitude, longitude);

            // Remplir le champ de départ avec le nom
            startField.setText(locationName);

            System.out.println("Position actuelle : " + locationName);
        } catch (Exception e) {
            System.err.println("Erreur lors de la détection de la position : " + e.getMessage());
        }
    }
    public void handlemeteo(ActionEvent actionEvent) {
        hideAllVBoxes();
        locationField.clear();
        meteoVBox.setManaged(true);
        meteoVBox.setVisible(true);
        //Utiliser les coordonnées stockées
        if (searchQuery != null) {
            locationField.setText(searchQuery);
            System.out.println("le lieu "+ searchQuery);
        } else {
            System.err.println("Erreur lors de la détection de la position ");

        }
    }

    public void handleDetectLocation(ActionEvent actionEvent) {
        try {

            // Récupérer les coordonnées de la localisation actuelle
            double[] selectedStartCoordinates = geolocationService.getLocationCoordinates();
            double latitude = selectedStartCoordinates[1];
            double longitude = selectedStartCoordinates[0];

            // Obtenir le nom de la position à partir des coordonnées
            String locationName = getLocationNameFromCoordinate(latitude, longitude);

            // Remplir le champ de départ avec le nom
            startField.setText(locationName);

            System.out.println("Position actuelle : " + locationName);
        } catch (Exception e) {
            System.err.println("Erreur lors de la détection de la position : " + e.getMessage());
        }

    }
    public void handleMeteo(ActionEvent actionEvent) {

        hideAllVBoxes();
        locationField.clear();
        meteoVBox.setManaged(true);
        meteoVBox.setVisible(true);

    }



    public void handleMeteoVBOX(ActionEvent actionEvent) {
        String locationQuery = locationField.getText();

        if (locationQuery != null && !locationQuery.isEmpty()) {
            // Utiliser la méthode getCoordinates pour obtenir les coordonnées du lieu
            double[] coordinates = getCoordinates(locationQuery);

            if (coordinates != null) {
                // Une fois les coordonnées obtenues, vous pouvez récupérer la météo
                getWeather(coordinates[0], coordinates[1], locationQuery);
            } else {
                // Afficher un message d'erreur si aucune coordonnée n'est trouvée
                System.out.println("Lieu non trouvé.");
            }
        } else {
            // Afficher un message d'erreur si aucun lieu n'est saisi
            System.out.println("Veuillez entrer un lieu.");
        }
    }
    private void getWeather(double latitude, double longitude, String locationQuery) {
        try {
            String apiKey = "c575c734e794f30a3f07b7018a965c1f"; // Votre clé API WeatherStack
            String url = "https://api.weatherstack.com/current?access_key=" + apiKey + "&query=" + latitude + "," + longitude;

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JSONObject weatherData = new JSONObject(jsonResponse);

                        // Vérifier si la réponse contient les informations météo
                        if (weatherData.has("current") && weatherData.has("location")) {
                            JSONObject currentWeather = weatherData.getJSONObject("current");
                            JSONObject locationInfo = weatherData.getJSONObject("location");

                            // Extraire les informations météo
                            String weatherDescription = currentWeather.getJSONArray("weather_descriptions").getString(0);
                            double temperature = currentWeather.getDouble("temperature");
                            String iconUrl = currentWeather.getJSONArray("weather_icons").getString(0);
                            double windSpeed = currentWeather.getDouble("wind_speed");
                            int humidity = currentWeather.getInt("humidity");
                            double feelsLike = currentWeather.getDouble("feelslike");

                            // Extraire la date et l'heure locale
                            String localtime = locationInfo.getString("localtime");

                            // Appeler showWeatherInfo pour afficher les informations dans le VBox de la météo
                            showWeatherInfo(locationQuery, localtime, weatherDescription, temperature, iconUrl, windSpeed, humidity, feelsLike);
                        } else {
                            System.out.println("Données météo non disponibles.");
                        }
                    } else {
                        System.err.println("Erreur lors de la requête météo : " + response.getStatusLine());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération de la météo : " + e.getMessage());
        }
    }

    private void showWeatherInfo(String locationName, String localtime, String description, double temperature, String iconUrl, double windSpeed, int humidity, double feelsLike) {
        // Appeler la méthode hideAllVBoxes pour cacher tous les autres éléments
        hideAllVBoxes();

        // Vérifier si le VBox a bien été trouvé
        if (meteoafficherVBox != null) {
            // Vider le contenu précédent
            meteoafficherVBox.getChildren().clear();

            // Créer un bouton de fermeture avec une icône
            Button closeButton = new Button();
            Image closeImage = new Image(getClass().getResource("/images/close-icon.png").toString());
            ImageView closeImageView = new ImageView(closeImage);
            closeImageView.setFitWidth(20);
            closeImageView.setFitHeight(20);

            closeButton.setGraphic(closeImageView);
            closeButton.setStyle(
                    "-fx-border-color: #0078d7;" +
                            "-fx-background-color: white; " +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 25; " +
                            "-fx-background-radius: 25; " +
                            "-fx-padding: 7; " +
                            "-fx-cursor: hand;"
            );

            // Action du bouton de fermeture
            closeButton.setOnAction(event -> {
                hideAllVBoxes();
                resetItineraryFields();
                itineraryFieldsVBox.setVisible(true);
                itineraryFieldsVBox.setManaged(true);
            });

            // Ajouter un espace en haut et en bas du bouton de fermeture
            VBox.setMargin(closeButton, new Insets(10, 0, 20, 0)); // 10px en haut et 20px en bas

            // Afficher le nom du lieu
            Label locationLabel = new Label("Lieu : " + locationName);
            locationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Afficher la date et l'heure locale
            Label localtimeLabel = new Label("Date et heure locale : " + localtime);
            localtimeLabel.setStyle("-fx-font-size: 14px;");

            VBox.setMargin(localtimeLabel, new Insets(0, 0, 5, 0)); // 5px en bas


            // Icônes et labels pour les autres valeurs
            ImageView temperatureIcon = new ImageView(getClass().getResource("/images/wi2.png").toString()); // icône température
            temperatureIcon.setFitWidth(30);
            temperatureIcon.setFitHeight(30);
            Label temperatureLabel = new Label(temperature + "°C");
            temperatureLabel.setStyle("-fx-font-size: 14px;");

            ImageView windIcon = new ImageView(getClass().getResource("/images/wi1.png").toString()); // icône vent
            windIcon.setFitWidth(30);
            windIcon.setFitHeight(30);
            Label windLabel = new Label(windSpeed + " km/h");
            windLabel.setStyle("-fx-font-size: 14px;");

            ImageView humidityIcon = new ImageView(getClass().getResource("/images/wi3.png").toString()); // icône humidité
            humidityIcon.setFitWidth(30);
            humidityIcon.setFitHeight(30);
            Label humidityLabel = new Label(humidity + "%");
            humidityLabel.setStyle("-fx-font-size: 14px;");

            // Créer un VBox pour chaque icône et sa valeur
            VBox temperatureVBox = new VBox(5);
            temperatureVBox.getChildren().addAll(temperatureIcon, temperatureLabel);

            VBox windVBox = new VBox(5);
            windVBox.getChildren().addAll(windIcon, windLabel);

            VBox humidityVBox = new VBox(5);
            humidityVBox.getChildren().addAll(humidityIcon, humidityLabel);

            // Créer un HBox pour organiser les VBoxes horizontalement
            HBox weatherInfoHBox = new HBox(15); // Espacement de 15 entre les éléments
            weatherInfoHBox.setAlignment(Pos.CENTER);
            weatherInfoHBox.getChildren().addAll(temperatureVBox, windVBox, humidityVBox);

            // Créer un VBox pour afficher le bouton de fermeture, le nom du lieu, la date et l'heure, et les informations météo
            VBox descriptionVBox = new VBox(10); // Espacement de 10 entre les éléments
            descriptionVBox.setAlignment(Pos.CENTER);
            descriptionVBox.getChildren().addAll(closeButton, locationLabel, localtimeLabel);

            // Ajouter tout dans le VBox principal
            meteoafficherVBox.getChildren().addAll(descriptionVBox, weatherInfoHBox);

            // Rendre le VBox visible
            meteoafficherVBox.setVisible(true);
            meteoafficherVBox.setManaged(true);
        } else {
            System.out.println("Le VBox 'meteoafficherVBox' n'a pas été trouvé.");
        }
    }



}