package com.project.internship_desk_booking_system.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_time_limits")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingTimeLimits {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_time_limit_seq")
    @SequenceGenerator(
            name = "booking_time_limit_seq",
            sequenceName = "booking_time_limit_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "max_days_in_advance", nullable = false)
    @Min(1)
    @Max(365)
    private Integer maxDaysInAdvance;

    @Column(name = "max_hours_per_week", nullable = false)
    @Min(1)
    @Max(168)
    private Integer maxHoursPerWeek;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
