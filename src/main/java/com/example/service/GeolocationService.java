 package com.example.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class GeolocationService {
    private static final String IPAPI_URL = "https://ipinfo.io/json";

    public double[] getLocationCoordinates() {
        double[] coordinates = new double[2]; // [latitude, longitude]
        try {
            URL url = new URL(IPAPI_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                // Parse JSON pour obtenir "loc" et séparer la latitude et la longitude
                JSONObject json = new JSONObject(result.toString());
                String loc = json.getString("loc"); // Récupérer la chaîne "loc"
                String[] latLng = loc.split(","); // Diviser pour obtenir latitude et longitude
                coordinates[0] = Double.parseDouble(latLng[1]); // Latitude
                coordinates[1] = Double.parseDouble(latLng[0]); // Longitude
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return coordinates;
    }
}