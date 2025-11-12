package com.project.internship_desk_booking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    DeskColorDTO deskColorDTO;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
