package com.example.service;

import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.List;

public class PolylineEncoder {

    /**
     * Implémentation manuelle du décodage de la polyligne.
     *
     * @param encodedPoints La chaîne encodée représentant les points de l'itinéraire.
     * @param precision      La précision utilisée pour le décodage (généralement 1e5 pour GraphHopper).
     * @return Une liste de points décodés sous forme de
     */
    public static List<GHPoint> decodePolyline(String encodedPoints, double precision) {
        List<GHPoint> decodedPoints = new ArrayList<>();
        int index = 0, len = encodedPoints.length();
        int lat = 0, lon = 0;

        while (index < len) {
            int b, shift = 0, result = 0;

            // Décodage de la latitude
            do {
                b = encodedPoints.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            // Décodage de la longitude
            shift = 0;
            result = 0;
            do {
                b = encodedPoints.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlon = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lon += dlon;

            // Ajouter un nouveau point décodé à la liste
            decodedPoints.add(new GHPoint(lat / precision, lon / precision));
        }

        return decodedPoints;
    }
}
