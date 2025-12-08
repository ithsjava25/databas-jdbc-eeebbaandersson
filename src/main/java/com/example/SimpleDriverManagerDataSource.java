package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDriverManagerDataSource implements DataSource{
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;

    public SimpleDriverManagerDataSource(String jdbcUrl, String dbUser, String dbPass) {
        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw  new IllegalArgumentException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }


    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
    }

    public void validateConnection(){
        try (Connection connection = getConnection()){
            if (connection != null){
                System.out.println("SUCCESS: Database connection established.");
            }

        } catch (SQLException e){
            throw new RuntimeException("FAILURE: Database connection not established.");

        }
    }

}
