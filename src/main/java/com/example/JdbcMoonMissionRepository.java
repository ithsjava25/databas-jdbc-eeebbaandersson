package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    // Tar emot DataSource-objekt via konstruktorn
    private final DataSource dataSource;

    public JdbcMoonMissionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public void listMoonMissions() {
        String query = "select spacecraft from moon_mission";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                String spacecraft = result.getString("spacecraft");
                System.out.println("spacecraft: " + spacecraft);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No available data found.");
        }
    }

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public void getMoonMissionById(String inputId) {
        String query = "select * from moon_mission where mission_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, inputId);
            ResultSet result = pstmt.executeQuery();

            while (result.next()) {
                int missionId = result.getInt("mission_id");
                String spacecraft = result.getString("spacecraft");
                java.util.Date date = result.getDate("launch_date");
                String carrierRocket = result.getString("carrier_rocket");
                String missionType = result.getString("mission_type");
                String outcome = result.getString("outcome");

                System.out.println("mission_id: " + missionId + " spacecraft: " + spacecraft);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Todo: Flytta output till main, får inte vara med här!
    @Override
    public void countMoonMissionByYear(String inputYear) {
        String query = "select count(*) as mission_launched from moon_mission where launch_date = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, Integer.parseInt(inputYear));
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                int count = result.getInt("mission_launched");
                System.out.println("Year: " + inputYear + " missions_launched: " + count);
            }

        } catch (SQLException e) {
            throw new RuntimeException("No data from selected year was found.");

        }

    }


}
