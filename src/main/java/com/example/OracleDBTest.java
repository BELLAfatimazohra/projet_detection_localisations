package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleDBTest {

    public static void testConnection() {
        String url = "jdbc:oracle:thin:@//localhost:1521/projet_detection_localisations";
        String username = "string";
        String password = "tiger";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            if (connection != null) {
                System.out.println("Connexion réussie à la base de données Oracle !");
            } else {
                System.out.println("Échec de la connexion.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
