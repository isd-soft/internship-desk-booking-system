package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.Desk;

import java.util.List;

/**
 * Service interface for comparing and copying Desk objects.
 */
public interface DeskDiffService {
    /**
     * Compares two Desk objects and returns a list of differences as strings.
     * @param before the original Desk
     * @param after the updated Desk
     * @return list of differences between before and after
     */
    List<String> diff(Desk before, Desk after);

    /**
     * Creates a deep copy of the given Desk object.
     * @param desk the Desk to copy
     * @return a new Desk object with the same properties
     */
    Desk copy(Desk desk);

}
