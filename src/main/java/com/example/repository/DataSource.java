package com.example.repository;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource {
    /**
 * Obtain a JDBC connection to the underlying data source.
 *
 * @return a JDBC {@link java.sql.Connection} to the underlying database
 * @throws java.sql.SQLException if a database access error occurs or a connection cannot be obtained
 */
Connection getConnection() throws SQLException;
}