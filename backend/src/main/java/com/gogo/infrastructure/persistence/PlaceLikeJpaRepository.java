package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.PlaceLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceLikeJpaRepository extends JpaRepository<PlaceLike, Long> {
    Optional<PlaceLike> findByUserIdAndPlaceId(Long userId, Long placeId);
    int countByPlaceId(Long placeId);
    void deleteByUserIdAndPlaceId(Long userId, Long placeId);
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
}
