package com.project.internship_desk_booking_system.entity;

import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "desk")
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "zone_id", nullable = false, foreignKey = @ForeignKey(name = "fk_desk_zone"))
    private Zone zone;

    @Column(name = "zone", nullable = false, length = 100)
    private String zoneName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DeskType type = DeskType.SHARED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeskStatus status = DeskStatus.ACTIVE;

    @Column(name = "is_temporarily_available", nullable = false)
    private Boolean isTemporarilyAvailable = false;

    @Column(name = "temporary_available_from")
    private LocalDateTime temporaryAvailableFrom;

    @Column(name = "temporary_available_until")
    private LocalDateTime temporaryAvailableUntil;

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
    public void setZone(Zone zone) {
        this.zone = zone;
        if (zone != null) {
            this.zoneName = zone.getZoneName();
        }
    }

}