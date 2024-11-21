package com.example.controller;

import com.example.service.GeolocationService;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class service {

    private WebView webView;

    public void showRestaurants() {
        System.out.println("Affichage des restaurants.");

        // Obtenir la localisation actuelle
        GeolocationService geolocationService = new GeolocationService();
        double[] coordinates = geolocationService.getLocationCoordinates();
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        // Réduire le rayon pour une recherche plus précise (5 km)
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node(around:5000," + longitude + "," + latitude + ")[amenity=restaurant];out;";

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
                System.out.println("Restaurants trouvés :");

                // Créer une liste pour les restaurants à envoyer au JavaScript
                StringBuilder restaurantLocations = new StringBuilder("[");

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject restaurant = elements.getJSONObject(i);
                    String name = restaurant.optJSONObject("tags").optString("name", "Nom inconnu");
                    double lat = restaurant.getDouble("lat");
                    double lon = restaurant.getDouble("lon");

                    // Affichage des détails du restaurant dans la console
                    System.out.println("Nom : " + name);
                    System.out.println("Latitude : " + lat);
                    System.out.println("Longitude : " + lon);
                    System.out.println("------------------------------");

                    // Ajouter les données du restaurant à la liste au format JSON
                    if (i > 0) {
                        restaurantLocations.append(",");
                    }
                    restaurantLocations.append("{")
                            .append("\"name\": \"").append(name).append("\",")
                            .append("\"lat\": ").append(lat).append(",")
                            .append("\"lng\": ").append(lon)
                            .append("}");
                }
                restaurantLocations.append("]");

                if (webView != null && webView.getEngine() != null) {
                    webView.getEngine().executeScript("updateMapWithRestaurants(" + restaurantLocations.toString() + ");");
                    System.out.println("Restaurants envoyés à la carte.");
                } else {
                    System.out.println("WebView ou son moteur non initialisé.");
                }

            } else {
                System.out.println("Aucun restaurant trouvé dans la zone.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
