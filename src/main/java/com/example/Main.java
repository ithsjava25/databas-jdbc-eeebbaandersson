package com.example;

import com.example.model.MoonMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private AccountRepository accountRepository;
    private MoonMissionRepository moonMissionRepository;

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw  new IllegalArgumentException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        // Skapar DataSource
       DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);

        try {
            if (dataSource instanceof SimpleDriverManagerDataSource sdmds) {
                sdmds.validateConnection();
                System.out.println("SUCCESS: Database connection established");
            }
        } catch (SQLException e) {
            throw new RuntimeException("FAILURE: Database connection not established.", e);
        }

        accountRepository = new JdbcAccountRepository(dataSource);
        moonMissionRepository = new JdbcMoonMissionRepository(dataSource);

        try (Scanner scanner = new Scanner(System.in)) {

            if (!handleUserLogin(scanner)) {
                System.out.println("Invalid username or password provided. Exiting the application.");
                return;
            }
            runMenuOptions(scanner);

        } catch (RuntimeException e) {
            System.out.println("FAILURE:  " + e.getMessage());
            throw new RuntimeException("Error while running application", e);
        }
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

    private boolean handleUserLogin(Scanner scanner) {
        System.out.println("To sign in please enter your information below:");

        while (true) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            if (username.equals("0")) {
                return false;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (password.equals("0")) {
                return false;
            }

            if (accountRepository.validateLogin(username, password)) {
                System.out.println("Login successful!");
                return true;
            } else {
                System.out.println("Login failed. Try again, or exit by pressing '0'.");

            }

        }
    }

    private void displayMenuOptions() {
        System.out.format(
                "   1| List moon missions. \n" +
                "   2| Get a moon mission by mission_id. \n" +
                "   3| Count missions for a given year. \n" +
                "   4| Create an account. \n" +
                "   5| Update an account password. \n" +
                "   6| Delete an account. \n" +
                "   0| Exit.");
        System.out.println(" ");
    }

    private void runMenuOptions(Scanner scanner) {

        System.out.println("--WELCOME TO THIS MOON MISSION APPLICATION! 🚀--");
        displayMenuOptions();

       while (true) {
           System.out.println("Enter your choice: ");
           String inputChoice = scanner.nextLine();

           try {
               switch (inputChoice) {
                   case "1" -> listMoonMissions();
                   case "2" -> getMoonMissionByID(scanner);
                   case "3" -> countMoonMissionsByYear(scanner);
                   case "4" -> createAccount(scanner);
                   case "5" -> updateAccountPassword(scanner);
                   case "6" -> deleteAccount(scanner);
                   case "0" -> {
                       System.out.println("Exiting the application.");
                       return;
                   }
                   default -> System.out.println("Invalid choice. Try again.");
               }
           } catch (RuntimeException e) {
               throw new RuntimeException("Error while running application", e);
           }


       }
    }

    private void listMoonMissions() {
        List<MoonMission> missions = moonMissionRepository.listMoonMissions();

        if(missions.isEmpty()) {
            System.out.println("No moon missions found!");
        } else {
            System.out.println("--MOON MISSION DETAILS--");
            missions.forEach(System.out::println);

        }
    }

    private void getMoonMissionByID(Scanner scanner) {
        System.out.println("Enter moon mission id: ");
        String inputId = scanner.nextLine().trim();

        try {
            int id = Integer.parseInt(inputId);

            Optional<MoonMission> mission = moonMissionRepository.getMoonMissionById(id);

            if (mission.isPresent()) {
                System.out.println("Moon mission found!");
                System.out.println(mission.get());
            } else {
                System.out.println("No mission found with id: " + inputId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Try again.");
        }
    }

    private void countMoonMissionsByYear(Scanner scanner) {
        System.out.println("Enter a year to find out number of missions launched: ");
        String inputYear = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(inputYear);
            int count = moonMissionRepository.countMoonMissionByYear(year);

            if (count > 0){
                System.out.println("Number of moon missions found for year " + year +": "+ count + ".");
            } else {
                System.out.println("No missions found for year " + year + ".");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    // Todo: Substring för att skapa username som ska in i databasen?
    private void createAccount(Scanner scanner) {
        System.out.println("To create a new account please enter your information below:");

        System.out.println("First name: ");
        String firstName = scanner.nextLine();
        System.out.println("Last name: ");
        String lastName = scanner.nextLine();
        System.out.println("Social Security Number (SSN): ");
        String ssn = scanner.nextLine();
        System.out.println("Password: ");
        String password = scanner.nextLine();

        if (firstName.isEmpty() || lastName.isEmpty() || ssn.isEmpty() || password.isEmpty()) {
            System.out.println("Invalid input. Try again.");
            return;
        }

       int rowsAffected = accountRepository.createAccount(firstName, lastName, ssn, password);

       if (rowsAffected > 0) {
           System.out.println("Account created successfully!");
       } else {
           System.out.println("Failed to create account.");
       }
    }

    private void updateAccountPassword(Scanner scanner) {
        // prompts: user_id, new password; prints confirmation
        System.out.println("To change password please enter your user id: ");
        String inputId = scanner.nextLine().trim();

        System.out.println("Enter your new password: ");
        String newPassword = scanner.nextLine().trim();

        if (newPassword.isEmpty()) {
            System.out.println("Error: Password must be provided.");
            return;
        }
        try {
            int userId = Integer.parseInt(inputId);
            boolean success = accountRepository.updateAccountPassword(newPassword, userId);

            if (success) {
                System.out.println("Account updated successfully!");
            } else {
                System.out.println("Failed to update account.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: User id must be a whole number.");
        } catch (RuntimeException e) {
            System.out.println("Error while updating account with id " + inputId + ": " + e.getMessage());
        }
    }

    private void deleteAccount(Scanner scanner) {
       // prompts: user_id; prints confirmation
        System.out.println("To delete account please enter your user id: ");
        String inputId = scanner.nextLine().trim();

        try {
            int userId = Integer.parseInt(inputId);
            boolean success = accountRepository.deleteAccount(userId);

            if (success) {
                System.out.println("Account deleted successfully!");
            } else  {
                System.out.println("Failed to delete account.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: User id must be a whole number.");
        } catch (RuntimeException e) {
            System.out.println("Error while deleting account with id " + inputId + ": " + e.getMessage());
        }
    }
 }






