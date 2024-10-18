package com.example.service;

import com.example.model.Location;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class LocationService {
    private List<Location> locations; // Liste pour stocker les localisations
    private SparkSession spark; // Spark Session

    public LocationService() {
        locations = new ArrayList<>();
        spark = SparkSession.builder()
                .appName("OSM Location Service")
                .master("local")
                .getOrCreate();
    }

    public void loadOSMData(String filePath) {
        try {
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("node");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getAttribute("id");
                    String lat = element.getAttribute("lat");
                    String lon = element.getAttribute("lon");
                    locations.add(new Location(id, Double.parseDouble(lat), Double.parseDouble(lon)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour rechercher une localisation
    public Dataset<Row> searchLocation(String locationName) {
        // Ici, tu devras adapter cette méthode pour effectuer une recherche
        // dans la liste 'locations' et retourner les coordonnées correspondantes.
        // Pour simplifier, on va juste simuler le retour d'une localisation.

        // Simuler une recherche pour l'exemple
        if (!locations.isEmpty()) {
            return spark.createDataFrame(List.of(locations.get(0)), Location.class).toDF();
        }
        return null; // Pas de résultats
    }
}
