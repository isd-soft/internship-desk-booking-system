package com.project.internship_desk_booking_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "favourite_desks")
@RequiredArgsConstructor
@NoArgsConstructor
public class Favourites {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator ="favourite_desk_seq"
    )
    @SequenceGenerator(
            name="favourite_desk_seq",
            sequenceName = "id_favourite_desk_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY ,
            optional = false
    )
    @JoinColumn(
            name="user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name="desk_id",
            nullable = false
    )
    private Desk desk;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favourites that = (Favourites) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
