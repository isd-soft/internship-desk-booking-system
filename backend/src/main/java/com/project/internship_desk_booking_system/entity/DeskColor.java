package com.project.internship_desk_booking_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "desk_color")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeskColor {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "color_id_seq")
    @SequenceGenerator(
            name = "color_id_seq",
            sequenceName = "color_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "color_name", nullable = false, unique = true)
    private String colorName;

    @Column(name = "color_code", nullable = false, unique = true)
    private String colorCode;

    @Column(name = "color_meaning", nullable = false, unique = true)
    private String colorMeaning;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeskColor deskColor = (DeskColor) o;
        return Objects.equals(id, deskColor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
