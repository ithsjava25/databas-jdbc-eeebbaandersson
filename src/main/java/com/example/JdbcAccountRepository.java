package com.example;

public class JdbcAccountRepository implements AccountRepository {

    // Tar emot DataSource-objekt via konstruktorn
    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Klassen ska hantera all JDBC/SQL-logik
    // Metoderna med Override


}
