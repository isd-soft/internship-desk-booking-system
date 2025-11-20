package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.Desk;

import java.util.List;

public interface DeskDiffService {
    List<String> diff(Desk before, Desk after);
    Desk copy(Desk desk);

}
