package com.gogo.domain.repository;

import com.gogo.domain.entity.PlaceLike;

import java.util.Optional;

public interface PlaceLikeRepository {
    PlaceLike save(PlaceLike like);
    void delete(Long userId, Long placeId);
    Optional<PlaceLike> findByUserIdAndPlaceId(Long userId, Long placeId);
    int countByPlaceId(Long placeId);
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
}
