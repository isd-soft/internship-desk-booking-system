package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.dto.DeskStatsDTO;
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
            "AND b.status = 'ACTIVE' " +
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

    long countByStartTimeAfter(LocalDateTime startTime);

    long countByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT new com.project.internship_desk_booking_system.dto.DeskStatsDTO(" +
            "d.id, d.deskName, d.zone, COUNT(b)) " +
            "FROM Booking b JOIN b.desk d " +
            "GROUP BY d.id, d.deskName, d.zone " +
            "ORDER BY COUNT(b) DESC")
    DeskStatsDTO findMostBookedDesk();

    @Query("SELECT new com.project.internship_desk_booking_system.dto.DeskStatsDTO(" +
            "d.id, d.deskName, d.zone, COUNT(b)) " +
            "FROM Booking b JOIN b.desk d " +
            "GROUP BY d.id, d.deskName, d.zone " +
            "ORDER BY COUNT(b) ASC")
    DeskStatsDTO findLeastBookedDesk();

    @Query("SELECT new com.project.internship_desk_booking_system.dto.DeskStatsDTO(" +
            "d.id, d.deskName, d.zone, COUNT(b)) " +
            "FROM Booking b JOIN b.desk d " +
            "WHERE b.startTime BETWEEN :startDate AND :endDate " +
            "GROUP BY d.id, d.deskName, d.zone " +
            "ORDER BY COUNT(b) DESC")
    DeskStatsDTO findMostBookedDeskInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.project.internship_desk_booking_system.dto.DeskStatsDTO(" +
            "d.id, d.deskName, d.zone, COUNT(b)) " +
            "FROM Booking b JOIN b.desk d " +
            "WHERE b.startTime BETWEEN :startDate AND :endDate " +
            "GROUP BY d.id, d.deskName, d.zone " +
            "ORDER BY COUNT(b) ASC")
    DeskStatsDTO findLeastBookedDeskInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}
