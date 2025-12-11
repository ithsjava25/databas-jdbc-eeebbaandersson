package com.example.repository;

import java.util.List;
import java.util.Optional;

public interface MoonMissionRepository {

    /**
 * Lists all moon mission names.
 *
 * @return a List of moon mission names; an empty list if no missions are available
 */
    List<String> listMoonMissions();
    /**
 * Retrieve a moon mission by its identifier.
 *
 * @param id the unique identifier of the moon mission
 * @return an Optional containing the MoonMission if found, otherwise an empty Optional
 */
Optional<MoonMission> getMoonMissionById(int id);
    /**
 * Count moon missions that occurred in the specified calendar year.
 *
 * @param year the calendar year to count missions for (for example, 1969)
 * @return the number of moon missions recorded for the specified year
 */
int countMoonMissionByYear(int year);
}