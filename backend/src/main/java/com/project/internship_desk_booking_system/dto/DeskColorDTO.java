package com.project.internship_desk_booking_system.dto;

import com.project.internship_desk_booking_system.enums.DeskColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeskColorDTO {
    private Long deskId;
    private DeskColor deskColor;
}
