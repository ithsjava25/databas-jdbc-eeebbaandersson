package com.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcAccountRepository implements AccountRepository {

    private final DataSource dataSource;

    /**
     * Create a JdbcAccountRepository backed by the provided DataSource.
     *
     * @param dataSource the DataSource used to obtain JDBC connections for repository operations
     */
    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Validate account credentials and retrieve the associated user ID.
     *
     * @param username the account name to authenticate
     * @param password the account password to authenticate
     * @return an Optional containing the matching user's `user_id` if credentials are valid, empty otherwise
     * @throws RuntimeException if a database error occurs during validation
     */
    @Override
    public Optional<Integer> validateLogin(String username, String password) {

        String query = "select user_id from account where name = ? and password = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1,username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    return Optional.of(rs.getInt("user_id"));
                }
                return Optional.empty();
            }
        } catch (SQLException e){
            throw new RuntimeException("Database error during login validation",e);
        }
    }


    /**
     * Inserts a new account record into the database's account table.
     *
     * @param firstName the account holder's first name
     * @param lastName  the account holder's last name
     * @param ssn       the account holder's social security number or national identifier
     * @param password  the account password
     * @return          the number of rows inserted (typically `1` if the account was created, `0` otherwise)
     */
    @Override
    public int createAccount(String firstName, String lastName, String ssn, String password) {

        String insert = "Insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(insert)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, ssn);
            pstmt.setString(4, password);

            return pstmt.executeUpdate();

        } catch (SQLException e){
            throw new RuntimeException("Error: Failed to create account",e);
        }
    }


    /**
     * Updates the stored password for the account identified by userId.
     *
     * @param newPassword the new password to set for the account
     * @param userId the identifier of the account to update
     * @return true if a database row was updated, false otherwise
     * @throws RuntimeException if a database error occurs while updating the password
     */
    @Override
    public boolean updateAccountPassword(String newPassword, int userId) {

        String update = "update account set password = ? where user_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(update)) {

            pstmt.setString(1, newPassword);
            pstmt.setInt(2, (userId));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error: Failed to update account with password.",e);
        }
    }

    /**
     * Deletes the account with the specified user_id from the database.
     *
     * @param userId the account's user_id to delete
     * @return `true` if an account row was deleted, `false` otherwise
     * @throws RuntimeException if a database error prevents the deletion
     */
    @Override
    public boolean deleteAccount(int  userId) {

        String delete = "delete from account where user_id = ?";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(delete)) {

            pstmt.setInt(1, (userId));

            int rowsAffected = pstmt.executeUpdate();
            return  rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account.",e);
        }
    }
}