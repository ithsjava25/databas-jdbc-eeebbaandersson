package com.example;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    // Tar emot DataSource-objekt via konstruktorn
    private final DataSource dataSource;

    public JdbcMoonMissionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Klassen ska hantera all JDBC/SQL-logik
    // Metoderna med Override
}
