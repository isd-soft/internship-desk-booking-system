package com.project.internship_desk_booking_system.entity;

import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "desk")
@RequiredArgsConstructor
public class Desk {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "desk_seq")
    @SequenceGenerator(
            name = "desk_seq",
            sequenceName = "id_desk_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "desk_name")
    private String deskName;

    @Column(name = "zone")
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DeskType type = DeskType.SHARED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeskStatus status = DeskStatus.ACTIVE;

    @Column(name = "is_temporarily_available", nullable = false)
    private Boolean isTemporarilyAvailable = false;

    @Column(name = "temporary_available_from")
    private LocalDateTime temporaryAvailableFrom;

    @Column(name = "temporary_available_until")
    private LocalDateTime temporaryAvailableUntil;

    public Desk(String deskName, String zone, DeskType type, DeskStatus status,
                Boolean isTemporarilyAvailable, LocalDateTime temporaryAvailableFrom,
                LocalDateTime temporaryAvailableUntil) {
        this.deskName = deskName;
        this.zone = zone;
        this.type = type;
        this.status = status;
        this.isTemporarilyAvailable = isTemporarilyAvailable;
        this.temporaryAvailableFrom = temporaryAvailableFrom;
        this.temporaryAvailableUntil = temporaryAvailableUntil;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Desk desk = (Desk) o;
        return Objects.equals(id, desk.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}