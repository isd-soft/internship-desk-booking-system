package com.project.internship_desk_booking_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "desk")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Desk {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "desk_seq")
    @SequenceGenerator(
            name = "desk_seq",
            sequenceName = "desk_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "desk_name", nullable = false, unique = true, length = 50)
    private String deskName;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false, length = 100)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeskType type = DeskType.SHARED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeskStatus status = DeskStatus.ACTIVE;

    @Column(name = "is_temporarily_available", nullable = false)
    private Boolean isTemporarilyAvailable = false;

    @Column(name = "temporary_available_from")
    private LocalDateTime temporaryAvailableFrom;

    @Column(name = "temporary_available_until")
    private LocalDateTime temporaryAvailableUntil;

    public boolean isAvailableForBooking() {
        if (status != DeskStatus.ACTIVE) {
            return false;
        }
        if (type == DeskType.SHARED) {
            return true;
        }
        if (type == DeskType.ASSIGNED) {
            return isTemporarilyAvailable
                    && temporaryAvailableFrom != null
                    && temporaryAvailableUntil != null
                    && LocalDateTime.now().isAfter(temporaryAvailableFrom)
                    && LocalDateTime.now().isBefore(temporaryAvailableUntil);
        }
        return false;
    }
}