package com.example.controller;

import ch.poole.geo.mbtiles4j.MBTilesReader;
import ch.poole.geo.mbtiles4j.MBTilesReadException;
import ch.poole.geo.mbtiles4j.Tile;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HomeController {

    @FXML
    private AnchorPane mapPane;

    private static final String MBTILES_FILE_PATH = "src/main/resources/tilles.mbtiles";

    public void initialize() throws IOException {
        int zoom = 5;
        int startTileX = 14;
        int startTileY = 9;
        int tileCount = 3;

        Path mbtilesPath = Paths.get(MBTILES_FILE_PATH);
        File mbtilesFile = mbtilesPath.toFile();

        // Vérifiez si le fichier MBTiles existe
        if (!mbtilesFile.exists()) {
            System.err.println("Le fichier MBTiles n'existe pas à l'emplacement : " + mbtilesFile.getAbsolutePath());
            return;
        }

        MBTilesReader mbtilesReader = null;
        try {
            mbtilesReader = new MBTilesReader(mbtilesFile);

            // Boucle pour récupérer et afficher les tuiles
            for (int x = 0; x < tileCount; x++) {
                for (int y = 0; y < tileCount; y++) {
                    Tile tile = mbtilesReader.getTile(startTileX + x, startTileY + y, zoom);

                    if (tile != null && tile.getData() != null) {
                        // Vérifiez que les données de la tuile ne sont pas nulles
                        byte[] tileData = tile.getData().readAllBytes();
                        Image tileImage = new Image(new ByteArrayInputStream(tileData));
                        ImageView tileView = new ImageView(tileImage);
                        tileView.setFitWidth(256);
                        tileView.setFitHeight(256);

                        // Positionnez la tuile sur le AnchorPane
                        AnchorPane.setLeftAnchor(tileView, x * 256.0);
                        AnchorPane.setTopAnchor(tileView, y * 256.0);

                        // Ajoutez la tuile au AnchorPane
                        mapPane.getChildren().add(tileView);
                    } else {
                        // Gestion des cas où la tuile ou ses données sont nulles
                        System.out.println("Tuile non trouvée ou données nulles pour x=" + (startTileX + x) + " y=" + (startTileY + y));
                    }
                }
            }
        } catch (IOException e) {
            // Gestion des exceptions d'entrée/sortie
            System.err.println("Erreur d'entrée/sortie lors du chargement des tuiles : " + e.getMessage());
            e.printStackTrace();
        } catch (MBTilesReadException e) {
            // Gestion des exceptions de lecture MBTiles
            System.err.println("Erreur lors de la lecture des MBTiles : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Assurez-vous de fermer le lecteur MBTiles
            if (mbtilesReader != null) {
                mbtilesReader.close(); // Décommentez si nécessaire
            }
        }
    }

    public void setUserDetails(String username, int userId) {
        // Logique pour utiliser le nom d'utilisateur et l'ID utilisateur
        System.out.println("Nom d'utilisateur: " + username + ", ID utilisateur: " + userId);
    }
}
