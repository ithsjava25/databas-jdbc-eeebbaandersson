package com.example;

import com.example.repository.MoonMission;
import com.example.repository.*;
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


    private Integer loggedInUserId;


    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * Application entry point that optionally initializes a development database and starts the main application.
     *
     * @param args command-line arguments; include "--dev" to enable development mode (also detected via VM option or DEV_MODE environment variable)
     */
    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    /**
     * Starts the application's runtime: initializes DB connections and repositories, performs user login, and enters the interactive menu loop.
     *
     * <p>The method resolves database configuration (system properties then environment variables), validates the connection,
     * instantiates repository implementations, prompts for user authentication, and, on successful login, runs the menu-driven interaction
     * until the user exits.</p>
     *
     * @throws IllegalArgumentException if required database configuration (APP_JDBC_URL, APP_DB_USER, APP_DB_PASS) is missing
     * @throws RuntimeException if the database connection cannot be established or if an unexpected runtime error occurs during execution
     */
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
            System.out.println("FAILURE: " + e.getMessage());
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
         * Resolve a configuration value using a Java system property first, then an environment variable.
         *
         * Trims surrounding whitespace and treats empty or whitespace-only values as absent.
         *
         * @param propertyKey the system property key to check first
         * @param envKey the environment variable name to check if the system property is absent or empty
         * @return the trimmed configuration value, or `null` if neither source provides a non-empty value
         */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    /**
     * Prompts the user to sign in and authenticates credentials from console input.
     *
     * Repeatedly requests a username and password from the provided scanner until
     * authentication succeeds or the user exits by entering "0" for either field.
     *
     * @param scanner the Scanner to read user input from (e.g., System.in)
     * @return `true` if authentication succeeded and the user was logged in, `false` if the user exited the login flow
     */
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

            Optional<Integer> userIdOptional = accountRepository.validateLogin(username, password);

            if (userIdOptional.isPresent()) {
                this.loggedInUserId = userIdOptional.get();
                System.out.println("Login successful!");
                return true;
            } else {
                System.out.println("Login failed. Please try again or exit by pressing '0'.");
            }
        }
    }

    /**
     * Prints the interactive menu of available application actions to standard output.
     *
     * <p>Menu includes options to list, retrieve, and count moon missions, manage accounts (create, update password, delete), and exit.</p>
     */
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

    /**
     * Display the interactive main menu and process user selections until the user exits.
     *
     * <p>Reads choices from the provided Scanner, dispatches the corresponding action for each menu option,
     * and returns when the user selects the exit option. Runtime exceptions thrown by menu actions are
     * caught and printed as error messages.</p>
     *
     * @param scanner the input Scanner used to read user choices (typically wrapping System.in)
     */
    private void runMenuOptions(Scanner scanner) {

        System.out.println("--WELCOME TO THIS MOON MISSION APPLICATION! 🚀--");
        displayMenuOptions();

       while (true) {
           System.out.println("Enter your choice: ");
           String inputChoice = scanner.nextLine();

           try {
               switch (inputChoice) {
                   case "1" -> listMoonMissions();
                   case "2" -> getMoonMissionById(scanner);
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
               System.out.println("Error: " + e.getMessage());
           }
       }
    }

    /**
     * Prints detailed fields of a MoonMission.
     *
     * @param mission the MoonMission to display; printed fields: missionId, spacecraft, launchDate, carrierRocket, operator, missionType, and outcome
     */
    private void displayMissionDetails(MoonMission mission) {
        System.out.println("--MOON MISSION DETAILS--");
        System.out.println("Mission ID: " + mission.missionId());
        System.out.println("Spacecraft: " + mission.spacecraft());
        System.out.println("Launch date: " + mission.launchDate());
        System.out.println("Carrier rocket: " + mission.carrierRocket());
        System.out.println("Operator: " + mission.operator());
        System.out.println("Mission type: " + mission.missionType());
        System.out.println("Outcome: " + mission.outcome());
    }

    /**
     * Prints moon mission spacecraft names to standard output; prints "No moon missions found!" when none are available.
     */
    private void listMoonMissions() {
        List<String> spacecrafts = moonMissionRepository.listMoonMissions();

        if(spacecrafts.isEmpty()) {
            System.out.println("No moon missions found!");
        } else {
            System.out.println("Moon missions:");
            spacecrafts.forEach(System.out::println);
        }
    }

    /**
     * Prompts the user for a moon mission id, retrieves that mission from the repository,
     * and displays its details if found.
     *
     * If no mission matches the entered id, prints a not-found message; if the input is not
     * a valid integer, prints an invalid input message.
     *
     * @param scanner the Scanner to read user input from
     */
    private void getMoonMissionById(Scanner scanner) {
        System.out.println("Enter moon mission id: ");
        String inputId = scanner.nextLine().trim();

        try {
            int id = Integer.parseInt(inputId);

            Optional<MoonMission> mission = moonMissionRepository.getMoonMissionById(id);

            if (mission.isPresent()) {
                displayMissionDetails(mission.get());
            } else {
                System.out.println("No mission found with id: " + inputId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Try again.");
        }
    }

    /**
     * Prompts the user for a year and displays how many moon missions were launched that year.
     *
     * Reads a line from the provided Scanner, parses it as an integer year, queries the repository
     * for the mission count, and prints either the count or a message indicating no missions were found.
     * If the input is not a valid integer, prints an "Invalid input." message.
     *
     * @param scanner the Scanner to read user input from
     */
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

    /**
     * Prompts the user for account details, validates the input, and creates the account when valid.
     *
     * <p>Validation performed:
     * <ul>
     *   <li>All fields must be non-empty.</li>
     *   <li>SSN must match the pattern "xxxxxx-xxxx".</li>
     *   <li>Password must be at least 6 characters long.</li>
     * </ul>
     * If validation succeeds, the method attempts to persist the account via the account repository and
     * prints success or failure messages. Validation failures and repository errors are reported to standard output.
     *
     * @param scanner the Scanner used to read user input
     */
    private void createAccount(Scanner scanner) {

        System.out.println("To create a new account please enter your information below:");

        System.out.println("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.println("Last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.println("Social Security Number (10 digits xxxxxx-xxxx): ");
        String ssn = scanner.nextLine();
        System.out.println("Password: ");
        String password = scanner.nextLine().trim();

        boolean isValid = true;

        if (firstName.isEmpty() || lastName.isEmpty() || ssn.isEmpty() || password.isEmpty()) {
            System.out.println("Error: Input fields can not be empty.");
            isValid = false;
        }

        if (isValid && !ssn.matches("\\d{6}-\\d{4}")) {
            System.out.println("Error: Invalid Social Security Number. Use format XXXX-XXXXX.");
            isValid = false;
        }

        if (isValid && password.length() < 6) {
            System.out.println("Error: Password must be at least 6 characters long.");
            isValid = false;
        }

        if (isValid) {
            try {
                int rowsAffected = accountRepository.createAccount(firstName, lastName, ssn, password);
                if (rowsAffected > 0) {
                    System.out.println("Account created successfully!");
                } else {
                    System.out.println("Failed to create account.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error while creating account: " + e.getMessage());
            }
        }
    }

    /**
     * Update an account's password after validating that a user is logged in and the provided input is valid.
     *
     * Prompts for a user id and a new password, verifies the caller is authenticated, ensures the password is provided
     * and at least 6 characters long, parses the user id as an integer, attempts to update the stored password, and
     * prints success or error messages for each outcome (validation failure, invalid id, or update failure).
     *
     * @param scanner the Scanner to read user input from
     */
    private void updateAccountPassword(Scanner scanner) {
//        // prompts: user_id, new password; prints confirmation
        if (this.loggedInUserId == null) {
            System.out.println("Error you must be logged in to update your password.");
            return;
        }

        System.out.println("To change password please enter your user id: ");
        String inputId = scanner.nextLine().trim();

        System.out.println("Enter your new password: ");
        String newPassword = scanner.nextLine().trim();

        if (newPassword.isEmpty()) {
            System.out.println("Error: Password must be provided.");
            return;
        }

        if (newPassword.length() < 6) {
            System.out.println("Password must be at least 6 characters long.");
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
            System.out.println("Error: Invalid input. User id must be a whole number.");

        }
    }

    /**
     * Deletes the account of the currently logged-in user after confirming the user id from input.
     *
     * If no user is logged in the method prints an error and returns. Prompts for a user id, attempts
     * to delete the account via the account repository, prints success or failure messages, and clears
     * the stored logged-in user id when deletion succeeds. Prints an error message for invalid numeric
     * input or repository errors.
     *
     * @param scanner the Scanner to read user input from (used to obtain the user id)
     */
    private void deleteAccount(Scanner scanner) {
       // prompts: user_id; prints confirmation
        if (this.loggedInUserId == null) {
            System.out.println("Error you must be logged in to delete your account.");
            return;
        }

        System.out.println("To delete account please enter your user id: ");
        String inputId = scanner.nextLine().trim();

        try {
            int userId = Integer.parseInt(inputId);
            boolean success = accountRepository.deleteAccount(userId);

            if (success) {
                System.out.println("Account deleted successfully!");
                this.loggedInUserId = null;
            } else  {
                System.out.println("Failed to delete account with id.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: User id must be a whole number.");
        } catch (RuntimeException e) {
            System.out.println("Error while deleting account" + e.getMessage());
        }
    }
 }





