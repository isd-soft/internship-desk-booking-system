package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.dto.DeskStatsProjection;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"user", "desk"})
    List<Booking> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    @EntityGraph(attributePaths = "desk")

    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(
            @Param("deskId") Long deskId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.startTime <= :endTime " +
            "AND b.endTime >= :startTime")
    List<Booking> findUserBookings(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Booking> findBookingsByUserOrderByStartTimeDesc(User user);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.user.id = :userId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime BETWEEN :now AND :hours " +
            "ORDER BY b.startTime ASC")
    List<Booking> findUpcomingBookingsWithin8Hours(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("hours") LocalDateTime Hours);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime >= :now " +
            "ORDER BY b.startTime ASC")
    List<Booking> findUpcomingBookingsByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

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

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.desk.id = :deskId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.endTime > :now")
    boolean existsActiveBookingsByDeskId(
            @Param("deskId") Long deskId,
            @Param("now") LocalDateTime now
    );

    long countBookingByStartTimeAfter(LocalDateTime startTime);

    long countByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = """
    SELECT d.desk_name AS deskName, COALESCE(COUNT(b.id), 0) AS bookingCount
    FROM desk d
    LEFT JOIN booking b ON b.desk_id = d.id
    GROUP BY d.id, d.desk_name
    ORDER BY bookingCount DESC
    LIMIT 1
""", nativeQuery = true)
    DeskStatsProjection findMostBookedDesk();
    @Query(value = """
    SELECT d.desk_name AS deskName, COUNT(b.id) AS bookingCount
    FROM desk d
    INNER JOIN booking b ON b.desk_id = d.id
    GROUP BY d.id, d.desk_name
    ORDER BY COUNT(b.id) ASC, d.desk_name ASC
    LIMIT 1
""", nativeQuery = true)
    DeskStatsProjection findLeastBookedDesk();

    @Query(value = """
    SELECT d.desk_name AS deskName, COUNT(b.id) AS bookingCount
    FROM desk d
    INNER JOIN booking b ON b.desk_id = d.id 
        AND b.start_time BETWEEN :startDate AND :endDate
    GROUP BY d.id, d.desk_name
    ORDER BY COUNT(b.id) ASC, d.desk_name ASC
    LIMIT 1
""", nativeQuery = true)
    DeskStatsProjection findLeastBookedDeskInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
    SELECT d.desk_name AS deskName, COUNT(b.id) AS bookingCount
    FROM desk d
    LEFT JOIN booking b ON b.desk_id = d.id AND b.start_time BETWEEN :startDate AND :endDate
    GROUP BY d.id, d.desk_name
    ORDER BY bookingCount DESC
    LIMIT 1
""", nativeQuery = true)
    DeskStatsProjection findMostBookedDeskInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}