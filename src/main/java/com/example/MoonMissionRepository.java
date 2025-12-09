package com.example;

import com.example.model.MoonMission;

import java.util.List;
import java.util.Optional;

public interface MoonMissionRepository {

    // Definierar metoder för affärslogik (finns just nu kvar i main)
    List<MoonMission> listMoonMissions();
    Optional<MoonMission> getMoonMissionById(int id);
    int countMoonMissionByYear(int year);
}
