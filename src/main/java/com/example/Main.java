package com.example;

import java.nio.file.attribute.UserPrincipal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        IO.println("--WELCOME TO THIS MOON MISSION APPLICATION! 🚀--");
//        String username = IO.readln("Username: ");
//        String password = IO.readln("Password: ");

        //Validation against account table (user + password) by calling method
         validateUserLogin(jdbcUrl, dbUser, dbPass);

        // Manage result from login-attempt:
        // if result = invalid display: "Invalid username or password" and option to exit program by entering "0"
        // if result = valid: move on and display application menu-options + add IO.readline() for userchoice!


//        if (arguments.length == 0) {
//            System.out.println("Missing arguments.");
//            return;
//        }


        // Move to switch (1-6+0) or new method like "manageMenuOptions"?

        // 1
        listMoonMissions(jdbcUrl, dbUser, dbPass);
        // 2
        getMoonMissionByID(jdbcUrl, dbUser, dbPass);
        // 3
        countMoonMissionsByYear(jdbcUrl, dbUser, dbPass);
        //4
        createAccount(jdbcUrl, dbUser, dbPass);
        // 5
        updateAccountPassword(jdbcUrl, dbUser, dbPass);
        // 6
        // 0

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
    public static void validateUserLogin(String jdbcUrl, String dbUser, String dbPass) {
        IO.println("Sign in by entering your information below:");
        String username = IO.readln("Username: ");
        String password = IO.readln("Password: ");

        String query = "select name, password from account where name = ? and password = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement pstmt = connection.prepareStatement(query)
        ) {
            pstmt.setString(1,username);
            pstmt.setString(2, password);

            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    System.out.println("Login successful! Welcome " + username + ".");
                } else {
                    System.out.println("Login failed. Invalid username or password provided.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void listMoonMissions(String jdbcUrl, String dbUser, String dbPass) {
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
            System.out.println("Error: Invalid input, please enter a valid number.");
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

    // SQL - INSERT
    // Använd int rowsAffected =  pstmt.executeUpdate() istället för (ResultSet result = pstmt.executeQuery())!
    public static void createAccount(String jdbcUrl, String dbUser, String dbPass){

        // prompts: first name, last name, ssn, password; prints confirmation
        String firstName = IO.readln("Enter first name: ");
        String lastName = IO.readln("Enter last name: ");
        String ssn  = IO.readln("Enter Social Security Number (SSN): ");
        String password = IO.readln("Enter password: ");

        // Insert into account ...
        String insert = "Insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement pstmt = connection.prepareStatement(insert)
        ) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, ssn);
            pstmt.setString(4, password);

           int rowsAffected =  pstmt.executeUpdate();
           if (rowsAffected > 0) {
               System.out.println("Account created successfully!");
           } else  {
               System.out.println("Error: Failed to create account.");
           }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // SQL - UPDATE
    // Använd int rowsAffected =  pstmt.executeUpdate() istället för (ResultSet result = pstmt.executeQuery())!
    // Todo : Failar mot test just nu!
    public static void updateAccountPassword(String jdbcUrl, String dbUser, String dbPass){
        // prompts: user_id, new password; prints confirmation
        int userId;
        String userIdInput;

        while (true) {
            userIdInput = IO.readln("Enter user ID: ");

            if (userIdInput == null) {
                System.out.println("Error: Input stream termined unexpectedly. Exiting.");
                return;

            }
            if (userIdInput.trim().isEmpty()) {
                System.out.println("Error: User ID must be provided.");
                continue;
            }

            try{
                userId = Integer.parseInt(userIdInput);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid User ID format. Please enter a number");
            }
        }

        String newPassword = IO.readln("Enter a new password : ");

        if  (newPassword == null || newPassword.trim().isEmpty()){
            System.out.println("Error: Password must be provided.");
            return;
        }

        String update = "update account set password = ? where user_id = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement pstmt = connection.prepareStatement(update)
        ) {

            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);

            int rowsAffected =  pstmt.executeUpdate();
           if (rowsAffected > 0) {
               System.out.println("Account updated successfully!");
           } else  {
               System.out.println("Error: Failed to update account. User ID " + userId + " might not exist.");
           }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // SQL - DELETE
    // Använd int rowsAffected =  pstmt.executeUpdate() istället för (ResultSet result = pstmt.executeQuery())!
    public static void deleteAccount(){
       // prompts: user_id; prints confirmation
    }
 }



