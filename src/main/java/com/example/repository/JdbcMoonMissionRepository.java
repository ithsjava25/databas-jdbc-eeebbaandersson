package com.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    private final DataSource dataSource;

    /**
     * Creates a JdbcMoonMissionRepository backed by the given DataSource.
     *
     * The provided DataSource is used to obtain database connections for repository operations.
     */
    public JdbcMoonMissionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Retrieve the spacecraft names from the moon_mission table.
     *
     * @return a list of spacecraft names from the moon_mission table; empty if no records are found
     * @throws RuntimeException if a database error occurs while fetching mission data
     */
    @Override
    public List<String> listMoonMissions() {
        List<String> spacecrafts = new ArrayList<>();

        String query = "select spacecraft from moon_mission";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
               spacecrafts.add(rs.getString("spacecraft"));
            }
            return  spacecrafts;
        } catch (SQLException e) {
            throw new RuntimeException("Error: Failed to fetch mission data from database", e);
        }
    }

    /**
     * Retrieve a moon mission by its mission identifier.
     *
     * The returned MoonMission will contain database column values for the matching record;
     * the launch date is set to null if the stored launch_date is null.
     *
     * @param id the mission_id of the moon mission to fetch
     * @return an Optional containing the matching MoonMission, or Optional.empty() if no record exists
     * @throws RuntimeException if a database error occurs while fetching the mission
     */
    @Override
    public Optional<MoonMission> getMoonMissionById(int id) {

        String query = "select mission_id, spacecraft, launch_date, carrier_rocket, operator, mission_type, outcome from moon_mission where mission_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()){
                if(rs.next()) {
                    MoonMission mission = new MoonMission(
                            rs.getInt("mission_id"),
                            rs.getString("spacecraft"),
                            rs.getDate("launch_date") != null
                                    ? rs.getDate("launch_date").toLocalDate() : null,
                            rs.getString("carrier_rocket"),
                            rs.getString("operator"),
                            rs.getString("mission_type"),
                            rs.getString("outcome")
                    );
                    return Optional.of(mission);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error: Failed to fetch mission data from database", e);
        }
    }

    /**
     * Count moon missions whose launch_date falls in the specified calendar year.
     *
     * @param year the calendar year to count missions for
     * @return the number of missions launched in the specified year
     * @throws RuntimeException if a database error occurs while performing the query
     */
    @Override
    public int countMoonMissionByYear(int  year) {

        String query = "select count(*) as mission_launched from moon_mission where year(launch_date) = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, year);

            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) {
                    return rs.getInt("mission_launched");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error counting missions for year " + year, e);

        }

    }



}