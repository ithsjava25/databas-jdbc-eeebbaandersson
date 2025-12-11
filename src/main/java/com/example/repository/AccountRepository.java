package com.example.repository;

import java.util.Optional;

public interface AccountRepository {

    /**
 * Validates the supplied credentials and locates the associated account.
 *
 * @param username the account username to validate
 * @param password the account password to validate
 * @return an Optional containing the account ID if the credentials are valid, empty otherwise
 */
    Optional<Integer> validateLogin(String username, String password);
    /**
 * Creates a new account with the provided personal details and credentials.
 *
 * @param firstName the account holder's given name
 * @param lastName  the account holder's family name
 * @param ssn       the account holder's Social Security Number
 * @param password  the account password
 * @return the identifier of the newly created account
 */
int createAccount(String firstName, String lastName, String ssn , String password);
    /**
 * Update the password for the account identified by the given userId.
 *
 * @param newPassword the new password to set for the account
 * @param userId the identifier of the account whose password will be updated
 * @return true if the password was updated successfully, false otherwise
 */
boolean updateAccountPassword(String newPassword, int userId);
    /**
 * Deletes the account with the specified user ID.
 *
 * @param userId the identifier of the account to delete
 * @return `true` if the account was deleted, `false` otherwise
 */
boolean deleteAccount(int userId);


}