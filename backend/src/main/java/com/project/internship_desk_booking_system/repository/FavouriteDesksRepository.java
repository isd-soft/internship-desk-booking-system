package com.project.internship_desk_booking_system.repository;

import com.project.internship_desk_booking_system.entity.FavouriteDesks;
import com.project.internship_desk_booking_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteDesksRepository extends JpaRepository<FavouriteDesks, Long> {

    List<FavouriteDesks> findByUser(User user);

    Optional<FavouriteDesks> findByUserIdAndDeskId(Long userId, Long deskId);

    void deleteByUserIdAndDeskId(Long userId, Long deskId);

    @Query("""
                select fd.user.email 
                from FavouriteDesks fd 
                where fd.desk.id = :deskId
            """)
    List<String> findEmailsByDeskId(Long deskId);

}
