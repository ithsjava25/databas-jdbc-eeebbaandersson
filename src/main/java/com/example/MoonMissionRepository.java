package com.example;

public interface MoonMissionRepository {

    // Definierar metoder för affärslogik (finns just nu kvar i main)
    void listMoonMissions();
    void getMoonMissionById(String inputId);
    void countMoonMissionByYear(String inputYear);
}
