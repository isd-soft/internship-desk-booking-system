package com.project.internship_desk_booking_system.entity;

import com.project.internship_desk_booking_system.enums.BookingStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@AllArgsConstructor
@Builder
@Table(name = "booking")
@NoArgsConstructor
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
    @SequenceGenerator(
            name = "booking_seq",
            sequenceName = "booking_id_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desk_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private Desk desk;

    @Column(name = "desk_id", insertable = false, updatable = false)
    private Long deskId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @PostLoad
    private void ensureDeskNotNull() {
        if (this.desk == null) {
            Desk placeholder = new Desk();
            placeholder.setId(-1L);
            placeholder.setDeskName("Deleted DESK");
            placeholder.setType(null);
            this.desk = placeholder;
        }
    }
}