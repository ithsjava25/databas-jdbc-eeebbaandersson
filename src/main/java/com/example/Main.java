package com.example;

import java.nio.file.attribute.UserPrincipal;
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

        // Prompt for Username/password on startup
        IO.println("--WELCOME TO THIS MOON MISSION APPLICATION! 🚀-- \n To sign in please enter your information below:");
        String username = IO.readln("Username: ");
        String password = IO.readln("Password: ");

        //Validation against account table (user + password) by calling method
        // validateUserLogin(jdbcUrl, dbUser, dbPass, username, password);

        // Manage result from login-attempt:
        // if result = invalid display: "Invalid username or password" and option to exit program by entering "0"
        // if result = valid: move on and display application menu-options



//        if (arguments.length == 0) {
//            System.out.println("Missing arguments.");
//            return;
//        }


        // Move into switch or separate method like "manageMenuOptions"?

        listMoonMissions(jdbcUrl, dbUser, dbPass);
        getMoonMissionByID(jdbcUrl, dbUser, dbPass);
        countMoonMissionsByYear(jdbcUrl, dbUser, dbPass);

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

    // Tar connection (3x variabler) + username/password som argument
    public static void validateUserLogin(String jdbcUrl, String dbUser, String dbPass, String username, String password) {
        // SQL-Fråga här?

        // finns det någon contains i inbyggt i SQL?
        //Annars en if-sats och räkna upp med equals?


    }


    private static void listMoonMissions(String jdbcUrl, String dbUser, String dbPass) {
        String query = "select spacecraft from moon_mission";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement pstmt = connection.prepareStatement(query)
        ) {

            try (ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    String spacecraft = result.getString("spacecraft");
                    System.out.println("spacecraft: " + spacecraft);
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

    public static void countMoonMissionsByYear(String jdbcUrl, String dbUser, String dbPass){
        String inputString = IO.readln("Enter a year between 1958-2019 to find out the number of missions launched that year: ");
        int inputYear;

        try {
            inputYear = Integer.parseInt(inputString);
            if (inputYear < 1958 || inputYear > 2019) {
                System.out.println("Error: Year must be between 1958-2019.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input, please enter a valid number");
            return;
        }

        String query = "select count(m.launch_date) as mission_launched " +
                "from moon_mission m " +
                "where year(m.launch_date) = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement pstmt = connection.prepareStatement(query)
        ) {

            pstmt.setInt(1, inputYear);

            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    int mission_launched = result.getInt("mission_launched");
                    System.out.println("year: " + inputYear + " missions_launched: " + mission_launched);
                } else {
                    System.out.println("No missions found for year: " + inputYear);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    public static void createAccount(){
        System.out.println("--Create Account--");
    }

    public static void updateAccount(){
        System.out.println("--Update Account--");
    }

    public static void deleteAccount(){
        System.out.println("--Delete Account--");
    }
 }



