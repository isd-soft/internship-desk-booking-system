package com.project.internship_desk_booking_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {
    private int minHours;
    private int maxHours;

    private int lunchStartHour;
    private int lunchEndHour;

    private int officeStartHour;
    private int officeEndHour;
}
