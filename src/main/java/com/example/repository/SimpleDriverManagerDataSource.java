package com.example.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDriverManagerDataSource implements DataSource {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;

    /**
     * Creates a data source that obtains connections from DriverManager using the provided JDBC URL and credentials.
     *
     * @param jdbcUrl the JDBC connection URL
     * @param dbUser  the database username
     * @param dbPass  the database password
     */
    public SimpleDriverManagerDataSource(String jdbcUrl, String dbUser, String dbPass) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    /**
     * Obtain a new JDBC Connection using the configured URL and credentials.
     *
     * @return a new open {@link java.sql.Connection} connected to the configured database
     * @throws SQLException if a database access error occurs or the URL or credentials are invalid
     */
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
    }

    /**
     * Verifies that a new JDBC connection can be established using this data source.
     *
     * Opens and immediately closes a connection to validate connectivity.
     *
     * @throws SQLException if a connection cannot be obtained; the exception's message is written to standard output before rethrowing
     */
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