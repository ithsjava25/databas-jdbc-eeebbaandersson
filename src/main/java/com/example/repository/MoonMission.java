package com.example.repository;

public record MoonMission(int missionId, String spacecraft, java.time.LocalDate launchDate,
                          String carrierRocket, String operator, String missionType, String outcome) {}
