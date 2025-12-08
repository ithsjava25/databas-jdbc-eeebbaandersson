package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    static void main(String[] args) throws SQLException {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() throws SQLException {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw  new IllegalArgumentException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            if (connection != null){
                System.out.println("SUCCESS: Database connection established.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("FAILURE: Database connection not established.");
        }

        //Todo: Starting point for your code

        Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);

        // Skapar DataSource-objekt
//        DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
//
//        // Testar databaanslutningen från SimpleDriverManagerDataSource
//        if (dataSource instanceof SimpleDriverManagerDataSource sdmds) {
//            sdmds.validateConnection();
//        }

        //Prompts for Username/password and validates them against table account (name+password)
        //Skickade tidigare in (jdbcUrl, dbUser, dbPass), för deklarering av Connection connection
         boolean result = validateUserLogin(connection);

        // Manages the result from login-attempt:
         if (result) {
             System.out.println("Login successful!");
             IO.println("--WELCOME TO THIS MOON MISSION APPLICATION! 🚀--");
             // Metod för att styra menu

         } else {
             System.out.println("Login failed. Invalid username or password provided.");
             // Todo: Add option to exit by pressing "0"
             // Program should exit..
         }

        // Todo: Add method call to display menu-options?
//         displayMenuOptions(connection);
//         System.console().readLine();

       // String userInput = IO.readln("Choose your option: ");
        // Move to switch (1-6+0)

        // 1
        listMoonMissions(connection);
        // 2
        getMoonMissionByID(connection);
        // 3
        countMoonMissionsByYear(connection);
        //4
        createAccount(connection);
        // 5
       // updateAccountPassword(connection);
        // 6
       // deleteAccount(connection);
        // 0
        // Exits program..

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

    public static boolean validateUserLogin(Connection connection) {
        IO.println("To sign in please enter your information below:");
        String username = IO.readln("Username: ");
        String password = IO.readln("Password: ");

        String query = "select name, password from account where name = ? and password = ?";

             try (PreparedStatement pstmt = connection.prepareStatement(query);
        ) {
            pstmt.setString(1,username);
            pstmt.setString(2, password);

            try (ResultSet result = pstmt.executeQuery()) {
                if (result.next()) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void displayMenuOptions(Connection connection) throws SQLException {

        System.out.format(
                "   1| List moon missions (prints spacecraft names from `moon_mission`).\n" +
                "   2| Get a moon mission by mission_id (prints details for that mission).\n" +
                "   3| Count missions for a given year (prompts: year; prints the number of missions launched that year).\n" +
                "   4| Create an account (prompts: first name, last name, ssn, password; prints confirmation).\n" +
                "   5| Update an account password (prompts: user_id, new password; prints confirmation).\n" +
                "   6| Delete an account (prompts: user_id; prints confirmation).\n" +
                "   0| Exit.");
    }

    public static void runMenu(Connection connection) throws SQLException {

    }

    // Todo: Addera visnig av alla kolumner!
    public static void listMoonMissions(Connection connection) throws SQLException {
        String query = "select spacecraft from moon_mission";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
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

    // Todo: Addera inläsning av ID input från användaren
    public static void getMoonMissionByID(Connection connection) throws SQLException {
        String query = "select mission_id, spacecraft from moon_mission";

        try (PreparedStatement pstmt = connection.prepareStatement(query);
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

    public static void countMoonMissionsByYear(Connection connection) throws SQLException {
        String inputString = IO.readln("Enter a year between 1958-2019 to find out the number of missions launched for that year: ");
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

        try (PreparedStatement pstmt = connection.prepareStatement(query);
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

    // För create/update/delete:
    // Använd int rowsAffected =  pstmt.executeUpdate() istället för (ResultSet result = pstmt.executeQuery())!
    public static void createAccount(Connection connection) throws SQLException {

        // prompts: first name, last name, ssn, password; prints confirmation
        String firstName = IO.readln("Enter first name: ");
        String lastName = IO.readln("Enter last name: ");
        String ssn  = IO.readln("Enter Social Security Number (SSN): ");
        String password = IO.readln("Enter password: ");

        // Insert into account ...
        String insert = "Insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insert);
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

    // Todo : Failar mot test just nu!
    public static void updateAccountPassword(Connection connection) throws SQLException {
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

            try {
                userId = Integer.parseInt(userIdInput);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid User ID format. Please enter a number.");
            }
        }

        String newPassword = IO.readln("Enter a new password : ");

        if  (newPassword == null || newPassword.trim().isEmpty()){
            System.out.println("Error: Password must be provided.");
            return;
        }

        String update = "update account set password = ? where user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(update);
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

    // Todo: Kommer antagligen faila på samma sätt som update ovan?
    public static void deleteAccount(Connection connection) throws SQLException {
       // prompts: user_id; prints confirmation

        //Behöver göras om till en int, men metoden parseInt kommer ge fel som i update-metoden?
        String userId = IO.readln("Enter user ID: ");

        String delete = "delete from account where user_id = ?";
    }
 }



