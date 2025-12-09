package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

//    private final AccountRepository accountRepository;
//    private final MoonMissionRepository moonMissionRepository;
//
//    public  Main(AccountRepository accountRepository, MoonMissionRepository moonMissionRepository) {
//        this.accountRepository = accountRepository;
//        this.moonMissionRepository = moonMissionRepository;
//    }

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

        try (Scanner scanner = new Scanner(System.in); Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
                System.out.println("SUCCESS: Database connection established.");

            if (!validateLogin(connection, scanner)) {
                System.out.println("Invalid username or password provided. Exiting the application.");
                return;
            }
            runMenuOptions(connection, scanner);

        } catch (SQLException e) {
            throw new RuntimeException("FAILURE: Database connection not established.");
        }

        //Todo: Starting point for your code

        // Skapar DataSource
       DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);

        // Testar databaanslutningen
        if (dataSource instanceof SimpleDriverManagerDataSource sdmds) {
            sdmds.validateConnection();
        }

        // Skapar Repositories
        AccountRepository accountRepo = new JdbcAccountRepository(dataSource);
        MoonMissionRepository moonMissionRepo = new JdbcMoonMissionRepository(dataSource);



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

//            if (accounRepository.validateLogin(username, password)) {
//                System.out.println("Login successful!");
//                return true;
//            } else {
//                System.out.println("Login failed. Try again, or exit by pressing '0'.");
//
//            }

        }
    }

    private boolean validateLogin(Connection connection, Scanner scanner) {
        System.out.println("To sign in please enter your information below:");

        boolean isValid = false;

        while (!isValid) {
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
            String query = "select name, password from account where name = ? and password = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(query)) {

                pstmt.setString(1, username);
                pstmt.setString(2, password);

                try (ResultSet result = pstmt.executeQuery()) {
                    if (result.next()) {
                        System.out.println("Login successful!");
                        return true;
                    } else {
                        System.out.println("Login failed. Try again, or exit by pressing '0'.");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return isValid;
    }

    // Behålla kvar här?
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
    private void runMenuOptions(Connection connection, Scanner scanner) throws SQLException {

        System.out.println("--WELCOME TO THIS MOON MISSION APPLICATION! 🚀--");
       displayMenuOptions();

       while (true) {
           System.out.println("Enter your choice: ");
           String inputChoice = scanner.nextLine();
           switch (inputChoice) {
               case "1" -> listMoonMissions(connection);
               case "2" -> getMoonMissionByID(connection, scanner);
               case "3" ->  countMoonMissionsByYear(connection, scanner);
               case "4" -> createAccount(connection, scanner);
               case "5" -> updateAccountPassword(connection, scanner);
               case "6" -> deleteAccount(connection, scanner);
               case "0" -> {
                   System.out.println("Exiting the application.");
                   return;
               }
               default -> System.out.println("Invalid choice. Try again.");
           }
       }
    }

    private void listMoonMissions(Connection connection) {
        String query = "select spacecraft from moon_mission";

            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet result = pstmt.executeQuery();
                while (result.next()) {
                    String spacecraft = result.getString("spacecraft");
                    System.out.println("spacecraft: " + spacecraft);
                }
        } catch (SQLException e) {
            throw new RuntimeException("No available data found.");
        }
    }

    // Todo: Fixa display av alla mission detaljer!
    private void getMoonMissionByID(Connection connection, Scanner scanner) {

        System.out.println("Enter moon mission id: ");
        String inputId = scanner.nextLine().trim();

        String query = "select * from moon_mission where mission_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, inputId);
            ResultSet result = pstmt.executeQuery();

                while (result.next()) {
                    int missionId = result.getInt("mission_id");
                    String spacecraft = result.getString("spacecraft");
                    java.util.Date date = result.getDate("launch_date");
                    String carrierRocket = result.getString("carrier_rocket");
                    String missionType = result.getString("mission_type");
                    String outcome = result.getString("outcome");

                    System.out.println("mission_id: " + missionId + " spacecraft: " + spacecraft);
                }


        } catch (SQLException e) {
            throw new RuntimeException(e);

    }
}

    private void countMoonMissionsByYear(Connection connection, Scanner scanner) {

        System.out.println("Enter a year between 1958-2019 to find out the number of missions launched for that year: ");
        String year = scanner.nextLine().trim();
        int inputYear  = Integer.parseInt(year);

        String query = "select count(*) as mission_launched from moon_mission where launch_date = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, inputYear);
            ResultSet result = pstmt.executeQuery();

                if (result.next()) {
                    int count = result.getInt("mission_launched");
                    System.out.println("Year: " + inputYear + " missions_launched: " + count);
                }

        } catch (SQLException e) {
            throw new RuntimeException("No data from selceted year was found.");

        }
    }

    // För create/update/delete:
    // Använd int rowsAffected =  pstmt.executeUpdate() istället för (ResultSet result = pstmt.executeQuery())!
    private void createAccount(Connection connection, Scanner scanner) {
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

    private void updateAccountPassword(Connection connection, Scanner scanner) {
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

    private void deleteAccount(Connection connection, Scanner scanner) {
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



