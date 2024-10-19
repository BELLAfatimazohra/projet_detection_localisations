package com.example.service;

import com.example.model.User;
import com.example.database.DatabaseConnection;

import java.sql.*;

public class UserService {

    // Méthode pour authentifier l'utilisateur
    public boolean authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String dbPassword = resultSet.getString("password");
                if (dbPassword.equals(password)) { // Comparer les mots de passe
                    return true;
                } else {
                    System.out.println("Mot de passe incorrect.");
                }
            } else {
                System.out.println("Nom d'utilisateur non trouvé.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Méthode pour récupérer l'ID de l'utilisateur
    public int getUserIdByUsername(String username) {
        String query = "SELECT id FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                System.out.println("Utilisateur non trouvé.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Retourne -1 en cas d'échec
    }

    // Méthode pour enregistrer un nouvel utilisateur
    public boolean register(User user) {
        String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword()); // Ajouter un hachage ici si nécessaire

            pstmt.executeUpdate();
            return true; // Inscription réussie
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Inscription échouée
    }

    // Méthode pour afficher le nom de la base de données
    public void printDatabaseName() {
        String query = "SELECT name FROM v$database";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                String dbName = resultSet.getString("NAME");
                System.out.println("Nom de la base de données: " + dbName);
            } else {
                System.out.println("Aucune donnée trouvée.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
