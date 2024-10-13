package com.example.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserService {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl"; // Remplacez par votre SID
    private static final String USER = "system";
    private static final String PASSWORD = "tiger";

    public boolean authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String dbPassword = resultSet.getString("password");
                // Comparez les mots de passe (ajustez si vous utilisez un hachage)
                if (dbPassword.equals(password)) {
                    return true; // Authentification réussie
                } else {
                    System.out.println("Mot de passe incorrect.");
                }
            } else {
                System.out.println("Nom d'utilisateur non trouvé.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public void printDatabaseName() {
        String query = "SELECT name FROM v$database"; // Cela récupère le nom de la base de données
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                String dbName = resultSet.getString("NAME");
                System.out.println("Nom de la base de données: " + dbName);
            } else {
                System.out.println("Aucune donnée trouvée pour le nom de la base de données.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
