package com.example;

import com.example.model.MoonMission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    // Tar emot DataSource-objekt via konstruktorn
    private final DataSource dataSource;

    public JdbcMoonMissionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<MoonMission> listMoonMissions() {
        List<MoonMission> missions = new ArrayList<>();

        String query = "select mission_id, spacecraft, launch_date, carrier_rocket, operator, mission_type, outcome from moon_mission";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                MoonMission mission = new MoonMission(
                        rs.getInt("mission_Id"),
                        rs.getString("spacecraft"),
                        rs.getString("launch_date"),
                        rs.getString("carrier_rocket"),
                        rs.getString("operator"),
                        rs.getString("mission_type"),
                        rs.getString("outcome")
                );
                missions.add(mission);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error: Failed to fetch mission data from database", e);
        }
        return missions;
    }

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public Optional<MoonMission> getMoonMissionById(int id) {

        String query = "select * from moon_mission where mission_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()){
                if(rs.next()) {
                    MoonMission mission = new MoonMission(
                            rs.getInt("mission_Id"),
                            rs.getString("spacecraft"),
                            rs.getString("launch_date"),
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

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public int countMoonMissionByYear(int  year) {

        String query = "select count(*) as mission_launched from moon_mission where launch_date = ?";

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
            throw new RuntimeException("Database error counting missions for year" + year, e);

        }

    }

}
