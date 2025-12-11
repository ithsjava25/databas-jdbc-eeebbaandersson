package com.example.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDriverManagerDataSource implements DataSource {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;

    public SimpleDriverManagerDataSource(String jdbcUrl, String dbUser, String dbPass) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
    }

    public void validateConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            // Connection successful
        }
        catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            throw e;
        }
    }

}
