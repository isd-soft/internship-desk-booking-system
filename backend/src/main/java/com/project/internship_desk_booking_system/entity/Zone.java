package com.project.internship_desk_booking_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "zone")
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zone_seq")
    @SequenceGenerator(
            name = "zone_seq",
            sequenceName = "zone_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "zone_name", nullable = false, unique = true, length = 100)
    private String zoneName;

    @Column(name = "zone_abbreviation", nullable = false, unique = true, length = 10)
    private String zoneAbv;


    public Zone() {}
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return Objects.equals(id, zone.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}