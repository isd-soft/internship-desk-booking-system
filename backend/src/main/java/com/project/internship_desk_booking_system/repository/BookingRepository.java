package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.dto.DeskStatsProjection;
import com.project.internship_desk_booking_system.entity.Booking;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByUserEmailAndId(String email, Long bookingId);

    List<Booking> findByUserEmailAndStatusOrderByStartTimeAsc(String email, BookingStatus status);

    @Modifying
    @Query("""
                UPDATE Booking b
                SET b.status = 'ACTIVE'
                WHERE b.status = 'SCHEDULED'
                  AND b.startTime <= :now
            """)
    int activateBookings(@Param("now") LocalDateTime now);

    @Modifying
    @Query("""
                UPDATE Booking b
                SET b.status = 'CONFIRMED'
                WHERE b.status = 'ACTIVE'
                  AND b.endTime <= :now
            """)
    int confirmBookings(@Param("now") LocalDateTime now);

    @Query("""
                SELECT COUNT(b) > 0 FROM Booking b
                WHERE b.user.id = :userId
                  AND b.status IN ('ACTIVE', 'SCHEDULED')
                  AND b.startTime < :endTime
                  AND b.endTime > :startTime
            """)
    boolean existsUserConflict(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );


    @Query("""
    SELECT COUNT(b) > 0 FROM Booking b
    WHERE b.desk.id = :deskId
      AND b.status <> 'CANCELLED'
      AND b.startTime < :endTime
      AND b.endTime > :startTime
""")
    boolean existsOverlappingBooking(
            Long deskId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId")
    List<Booking> findBookingsByDeskId(Long deskId);


    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.status IN ('SCHEDULED', 'ACTIVE', 'CONFIRMED') " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime")
    List<Booking> findUserBookings(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Booking> findBookingsByUserOrderByStartTimeDesc(User user);



    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.desk.id = :deskId
              AND b.status IN ('SCHEDULED', 'ACTIVE', 'CONFIRMED')
              AND b.startTime < :workdayEnd
              AND b.endTime > :workdayStart
            """)
    List<Booking> findDeskBookingsForDay(
            @Param("deskId") Long deskId,
            @Param("workdayStart") LocalDateTime workdayStart,
            @Param("workdayEnd") LocalDateTime workdayEnd
    );


    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.desk.id = :deskId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.endTime > :now")
    boolean existsActiveBookingsByDeskId(
            @Param("deskId") Long deskId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.desk.id = :deskId " +
            "AND b.status = 'SCHEDULED' " +
            "AND b.endTime > :now")
    boolean existsScheduledBookingsByDeskId(
            @Param("deskId") Long deskId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId AND b.status = 'SCHEDULED' AND b.startTime > CURRENT_TIMESTAMP")
    List<Booking> findScheduledBookingsByDeskId(@Param("deskId") Long deskId);

    long countBookingByStartTimeAfter(LocalDateTime startTime);

    long countByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = """
                SELECT d.desk_name AS deskName, COALESCE(COUNT(b.id), 0) AS bookingCount
                FROM desk d
                LEFT JOIN booking b ON b.desk_id = d.id
                GROUP BY d.id, d.desk_name
                HAVING COUNT(b.id) >= 1
                ORDER BY COUNT(b.id) DESC, d.desk_name ASC
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
                HAVING COUNT(b.id) >= 1
                ORDER BY COUNT(b.id) DESC, d.desk_name ASC
                LIMIT 1
            """, nativeQuery = true)
    DeskStatsProjection findMostBookedDeskInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT b
            FROM Booking b
            WHERE DATE(b.startTime) = :localDate
            """)
    List<Booking> findBookingsByDate(
            @Param("localDate") LocalDate localDate
    );

    @Query("""
            SELECT b
            FROM Booking b
            WHERE DATE(b.startTime) = :localDate
                AND b.user.id = :userId
                AND b.status != 'CANCELLED'
            """)
    List<Booking> findUserBookingsByDateNotCancelled(
            @Param("userId") Long userId,
            @Param("localDate") LocalDate localDate
    );

    @Query(value = """
                SELECT DATE(b.start_time) as date, COUNT(b.id) as count
                FROM booking b
                WHERE b.start_time BETWEEN :startDate AND :endDate
                GROUP BY DATE(b.start_time)
                ORDER BY DATE(b.start_time)
            """, nativeQuery = true)
    List<Object[]> countBookingsGroupedByDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
                SELECT DATE_TRUNC('week', b.start_time)::date as weekStart, 
                       COUNT(b.id) as count
                FROM booking b
                WHERE b.start_time BETWEEN :startDate AND :endDate
                GROUP BY DATE_TRUNC('week', b.start_time)
                ORDER BY weekStart
            """, nativeQuery = true)
    List<Object[]> countBookingsGroupedByWeek(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Optional<Booking> findByDeskId(Long deskId);

    @Modifying
    @Query("""
            UPDATE Booking b 
            SET b.status = 'CANCELLED' 
            WHERE b.desk.id = :deskId 
            AND b.status = 'ACTIVE' 
            AND b.endTime > CURRENT_TIMESTAMP
            """)
    void cancelAllActiveBookingsForDesk(@Param("deskId") Long deskId);

    @Modifying
    @Query("""
            UPDATE Booking b 
            SET b.status = 'CANCELLED' 
            WHERE b.desk.id = :deskId 
            AND b.status = 'SCHEDULED' 
            AND b.endTime > CURRENT_TIMESTAMP
            """)
    void cancelAllPendingBookingsForDesk(@Param("deskId") Long deskId);


    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId AND b.status = 'ACTIVE'")
    List<Booking> findActiveBookingsForDesk(@Param("deskId") Long deskId);

    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId AND b.status = 'PENDING'")
    List<Booking> findPendingBookingsForDesk(@Param("deskId") Long deskId);

}