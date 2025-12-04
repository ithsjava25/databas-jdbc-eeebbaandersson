package com.example;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {

    // Fält som lagrar argument
    private final String[] arguments;

    // Konstruktor för att tar emot argumenten
    public Main(String[] arguments) {
        this.arguments = arguments;
    }

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main(args).run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
           System.out.println("SUCCESS: Database connection established.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo: Starting point for your code

        // Prompt för Username/password
        //Validate them against account table (user + password)

        //listMoonMissions(jdbcUrl, dbUser, dbPass);
        getMoonMissionByID(jdbcUrl, dbUser, dbPass);



//        if (arguments.length == 0) {
//            System.out.println("Missing arguments.");
//            return;
//        }

    }

    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    public static void greetUser(){
        System.out.println("--Welcome to this MoonMission Application!--");
    }


    private static void listMoonMissions(String jdbcUrl, String dbUser, String dbPass) {
        String query = "select spacecraft from moon_mission";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement pstmt = connection.prepareStatement(query)
        ) {

            try (ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    System.out.println(result.getString("spacecraft"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getMoonMissionByID(String jdbcUrl, String dbUser, String dbPass){
        String query = "select mission_id, spacecraft from moon_mission";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement pstmt = connection.prepareStatement(query)
        ) {
            try (ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt("mission_id");
                    String spacecraft = result.getString("spacecraft");
                    System.out.println("mission_id: " + id + " spacecraft: " + spacecraft);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);

    }
}

    public static void countMoonMissionsByYear(){
        System.out.println("--Count Moon Missions by Year--");

    }

    public static void createAccount(){
        System.out.println("--Create Account--");
    }

    public static void updateAccount(){
        System.out.println("--Create Account--");
    }

    public static void deleteAccount(){
        System.out.println("--Delete Account--");
    }
 }



