package com.example.controller;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.geom.GeoPosition; // Import de GeoPosition
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import com.example.service.LocationService;


public class HomeController {

    @FXML
    private VBox mapContainer;

    @FXML
    private TextField searchField;

    private JXMapViewer mapViewer; // Déclaration du JXMapViewer en tant que variable de classe

    // Méthode appelée à l'initialisation du contrôleur
    public void initialize() {
        // Initialisation de la carte
        initializeMap();
    }

    // Initialiser la carte dans le conteneur
    private void initializeMap() {
        mapViewer = new JXMapViewer(); // Crée un nouveau JXMapViewer

        // Configure le TileFactory avec OpenStreetMap
        TileFactoryInfo info = new TileFactoryInfo(1, 17, 17,
                256, true, true, "https://tile.openstreetmap.org",
                "x", "y", "z");
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Embedding Swing node into JavaFX
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(mapViewer);
        mapContainer.getChildren().add(swingNode);
    }

    // Méthode appelée lorsque le bouton "Rechercher" est cliqué
    @FXML
    public void onSearchButtonClick() {
        String locationName = searchField.getText();
        if (locationName != null && !locationName.isEmpty()) {
            searchLocation(locationName);
        }
    }

    // Recherche une localisation via le service de localisation
    private void searchLocation(String locationName) {
        LocationService locationService = new LocationService();
        Dataset<Row> result = locationService.searchLocation(locationName);

        // Gérer les résultats et mettre à jour la carte
        if (result != null && result.count() > 0) {
            // Par exemple, vous pouvez obtenir les coordonnées et centrer la carte
            double latitude = result.first().getDouble(0); // Ajustez l'index selon la structure de vos données
            double longitude = result.first().getDouble(1); // Ajustez l'index selon la structure de vos données
            mapViewer.setAddressLocation(new GeoPosition(latitude, longitude)); // Centrer la carte sur la localisation
        } else {
            // Gérer le cas où aucune localisation n'est trouvée
            System.out.println("Aucune localisation trouvée pour : " + locationName);
        }
    }
}
