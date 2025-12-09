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

    public void main(String[] args) throws SQLException {
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


        //Todo: Starting point for your code

        // Skapar DataSource
       DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        this.accountRepository = new JdbcAccountRepository(dataSource);
        this.moonMissionRepository = new JdbcMoonMissionRepository(dataSource);



        try (Scanner scanner = new Scanner(System.in)) {
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("SUCCESS: Database connection established.");

            }

            if (!handleUserLogin(scanner)) {
                System.out.println("Invalid username or password provided. Exiting the application.");
                return;
            }
            runMenuOptions(accountRepository, moonMissionRepository, scanner);

        } catch (SQLException e) {
            throw new RuntimeException("FAILURE: Database connection not established.");
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

    // Behålla kvar här?
    private void runMenuOptions(AccountRepository accountRepository, MoonMissionRepository moonMissionRepository, Scanner scanner) {

        System.out.println("--WELCOME TO THIS MOON MISSION APPLICATION! 🚀--");
        displayMenuOptions();

       while (true) {
           System.out.println("Enter your choice: ");
           String inputChoice = scanner.nextLine();

           try {
               switch (inputChoice) {
                   case "1" -> listMoonMissions();
                   case "2" -> getMoonMissionByID(scanner);
                   case "3" -> countMoonMissionsByYear(moonMissionRepository,scanner);
                   case "4" -> createAccount(accountRepository,scanner);
                   case "5" -> updateAccountPassword(accountRepository, scanner);
                   case "6" -> deleteAccount(accountRepository, scanner);
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


    private void countMoonMissionsByYear(MoonMissionRepository moonMissionRepository, Scanner scanner) {
        System.out.println("Enter a year to find out number of missions launched: ");
        String inputYear = scanner.nextLine().trim();

        try {
            int  year = Integer.parseInt(inputYear);
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

    // För create/update/delete:
    // Använd int rowsAffected =  pstmt.executeUpdate() istället för (ResultSet result = pstmt.executeQuery())!
    private void createAccount(AccountRepository accountRepository, Scanner scanner) {
        System.out.println("To create a new account please enter your information below:");

        System.out.println("First name: ");
        String firstName = scanner.nextLine();
        System.out.println("Last name: ");
        String lastName = scanner.nextLine();
        System.out.println("Social Security Number (SSN): ");
        String ssn = scanner.nextLine();
        System.out.println("Password: ");
        String password = scanner.nextLine();

        // Insert into account ...
        String insert = "Insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insert)) {
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

    private void updateAccountPassword(AccountRepository accountRepository, Scanner scanner) {
        // prompts: user_id, new password; prints confirmation
        System.out.println("To change password please enter your user id: ");
        int userId = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter your new password: ");
        String newPassword = scanner.nextLine();

        if  (newPassword == null || newPassword.trim().isEmpty()){
            System.out.println("Error: Password must be provided.");
            return;
        }
        String update = "update account set password = ? where user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(update)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, (userId));

            int rowsAffected =  pstmt.executeUpdate();
           if (rowsAffected > 0) {
               System.out.println("Account updated successfully!");
           }

        } catch (SQLException e) {
            throw new RuntimeException("Error: Failed to update account with password.");
        }
    }

    private void deleteAccount(AccountRepository accountRepository, Scanner scanner) {
       // prompts: user_id; prints confirmation

        System.out.println("To delete account please enter your user id: ");
        int userId = Integer.parseInt(scanner.nextLine());

        //addera validering?

        String delete = "delete from account where user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(delete)) {
            pstmt.setInt(1, (userId));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account deleted successfully!");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account.");
        }
    }

 }






