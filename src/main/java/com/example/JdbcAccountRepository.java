package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcAccountRepository implements AccountRepository {

    // Tar emot DataSource-objekt via konstruktorn
    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean validateLogin(String username, String password) {

        String query = "select name, password from account where name = ? and password = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1,username);
            pstmt.setString(2, password);

            try (ResultSet resultSet = pstmt.executeQuery()){
                return resultSet.next();
            }
        } catch (SQLException e){
            throw new RuntimeException("Error: failed to validate user login");
        }
    }

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public int createAccount(String firstName, String lastName, String ssn , String password) {
        String insert = "Insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(insert)) {

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

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public void updateAccount(String newPassword, int userId) {
        String update = "update account set password = ? where user_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(update)) {

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

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public int deleteAccount(int  userId) {
        String delete = "delete from account where user_id = ?";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(delete)) {
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
