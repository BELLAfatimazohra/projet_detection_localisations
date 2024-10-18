package com.example.service;
import org.apache.spark.sql.SparkSession;
import org.apache.sedona.sql.utils.SedonaSQLRegistrator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
public class LocationService {
    private SparkSession spark;
    public LocationService() {
        this.spark = SparkSession.builder()
                .appName("SedonaApp")
                .config("spark.master", "local")
                .getOrCreate();
        // Enregistre Sedona avec Spark SQL
        SedonaSQLRegistrator.registerAll(spark);
    }
    // Méthode pour rechercher une localisation dans les données géographiques
    public Dataset<Row> searchLocation(String locationName) {
        // Exécuter une requête géospatiale pour rechercher une localisation
        Dataset<Row> locations = spark.sql("SELECT * FROM location_table WHERE name LIKE '%" + locationName + "%'");
        return locations;
    }
}
