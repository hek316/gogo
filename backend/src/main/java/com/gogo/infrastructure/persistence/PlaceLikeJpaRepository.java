package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.PlaceLikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceLikeJpaRepository extends JpaRepository<PlaceLikeJpaEntity, Long> {
    Optional<PlaceLikeJpaEntity> findByUserIdAndPlaceId(Long userId, Long placeId);
    int countByPlaceId(Long placeId);
    void deleteByUserIdAndPlaceId(Long userId, Long placeId);
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
}
