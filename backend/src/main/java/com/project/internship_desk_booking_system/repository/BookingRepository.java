package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<List<Booking>> findAllByUserEmail(String email);

    //find all bookings made by a user
    List<Booking> findByUser_idAndStatus(int user_id, String status);

    //find all active bookings for a single desk in a time range(for overlapping)
    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(
            @Param("deskId") Long deskId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    //find users bookings in a time range
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime <= :endTime " +
            "AND b.endTime >= :startTime")
    List<Booking> findUserBookings(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    //find all the bookings for a specific desk
    //List<Booking> findBookingsForASpecificDesk(Long deskId, Booking status);

    //find the future bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime = :now " +
            "ORDER BY b.startTime ASC")
    List<Booking> findFutureBookings(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    // Find user's bookings in a specific time range
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime >= :startTime " +
            "AND b.endTime <= :endTime")
    List<Booking> findUserBookingsInTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Find upcoming bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime >= :now " +
            "ORDER BY b.startTime ASC")
    List<Booking> findUpcomingBookingsByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // find all active bookings (for admin)
    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' " +
            "AND b.endTime > :now " +
            "ORDER BY b.startTime ASC")
    List<Booking> findAllActiveBookings(@Param("now") LocalDateTime now);

    @Modifying
    @Query(
            value = """
                    DELETE FROM booking WHERE
                    id = :deskId AND
                    user_id = :userId""",
            nativeQuery = true)
    void deleteBookingByDeskId(
            @Param("deskId") Long deskId,
            @Param("userId") Long userId);
}
