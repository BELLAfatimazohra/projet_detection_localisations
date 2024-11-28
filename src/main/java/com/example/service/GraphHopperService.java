package com.example.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class GraphHopperService {
    private static final String API_URL = "https://graphhopper.com/api/1/route";
    private final String apiKey;

    public GraphHopperService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Calcule l'itinéraire entre deux points pour un mode de transport donné
     */
    public String calculateRoute(double startLat, double startLon, double endLat, double endLon, String vehicle) throws IOException {
        String url = String.format("%s?point=%f,%f&point=%f,%f&type=json&locale=en&vehicle=%s&key=%s",
                API_URL, startLat, startLon, endLat, endLon, vehicle, apiKey);

        System.out.println("URL API GraphHopper : " + url);


        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            // Envoyer la requête et récupérer la réponse
            HttpEntity entity = client.execute(request).getEntity();
            String responseString = EntityUtils.toString(entity);

            // Traiter la réponse JSON pour extraire les coordonnées
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseString);
            JsonNode pathNode = rootNode.path("paths");

            if (pathNode.isArray() && pathNode.size() > 0) {
                JsonNode path = pathNode.get(0);
                return extractCoordinatesForJS(path);
            } else {
                throw new RuntimeException("Aucune route trouvée.");
            }
        }
    }


    private String extractCoordinatesForJS(JsonNode path) {
        JsonNode points = path.path("points").path("coordinates");

        StringBuilder routeCoordinates = new StringBuilder("[");
        for (JsonNode point : points) {
            double lon = point.get(0).asDouble();
            double lat = point.get(1).asDouble();
            routeCoordinates.append("[").append(lon).append(",").append(lat).append("],");
        }

        if (routeCoordinates.length() > 1) {
            routeCoordinates.setLength(routeCoordinates.length() - 1);
        }
        routeCoordinates.append("]");

        return routeCoordinates.toString();
    }
}
