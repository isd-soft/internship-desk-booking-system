package com.project.internship_desk_booking_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for booking rules and office hours.
 * <p>
 * Properties are loaded from application configuration with prefix 'booking'.
 * </p>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {
    /**
     * Minimum allowed booking duration in hours.
     */
    private int minHours;
    /**
     * Maximum allowed booking duration in hours.
     */
    private int maxHours;
    /**
     * Start hour of lunch break (24h format).
     */
    private int lunchStartHour;
    /**
     * End hour of lunch break (24h format).
     */
    private int lunchEndHour;
    /**
     * Office workday start hour (24h format).
     */
    private int officeStartHour;
    /**
     * Office workday end hour (24h format).
     */
    private int officeEndHour;
}
