package com.gogo.db.repository;

import com.gogo.db.entity.PlaceLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaceLikeRepository extends JpaRepository<PlaceLike, Long> {
    Optional<PlaceLike> findByUserIdAndPlaceId(Long userId, Long placeId);
    int countByPlaceId(Long placeId);
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    @Modifying
    @Query("DELETE FROM PlaceLike pl WHERE pl.userId = :userId AND pl.placeId = :placeId")
    void deleteByUserIdAndPlaceId(@Param("userId") Long userId, @Param("placeId") Long placeId);
}
