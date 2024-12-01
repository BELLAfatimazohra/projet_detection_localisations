package com.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.service.PolylineEncoder;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
                return decodePolylineForJS(path);
            } else {
                throw new RuntimeException("Aucune route trouvée.");
            }
        }
    }

    /**
     * Décode les points encodés et les formate pour JavaScript
     */
    private String decodePolylineForJS(JsonNode path) {
        String encodedPoints = path.path("points").asText();
        List<GHPoint> decodedPoints = com.example.service.PolylineEncoder.decodePolyline(encodedPoints, 1e5);

        StringBuilder routeCoordinates = new StringBuilder("[");
        for (GHPoint point : decodedPoints) {
            routeCoordinates.append("[").append(point.lon).append(",").append(point.lat).append("],");
        }

        if (routeCoordinates.length() > 1) {
            routeCoordinates.setLength(routeCoordinates.length() - 1); // Supprimer la dernière virgule
        }
        routeCoordinates.append("]");

        return routeCoordinates.toString();
    }
}
