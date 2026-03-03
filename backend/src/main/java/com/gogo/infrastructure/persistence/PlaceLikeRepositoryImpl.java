package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.PlaceLike;
import com.gogo.domain.repository.PlaceLikeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class PlaceLikeRepositoryImpl implements PlaceLikeRepository {

    private final PlaceLikeJpaRepository jpa;

    public PlaceLikeRepositoryImpl(PlaceLikeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PlaceLike save(PlaceLike like) {
        return jpa.save(like);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long placeId) {
        jpa.deleteByUserIdAndPlaceId(userId, placeId);
    }

    @Override
    public Optional<PlaceLike> findByUserIdAndPlaceId(Long userId, Long placeId) {
        return jpa.findByUserIdAndPlaceId(userId, placeId);
    }

    @Override
    public int countByPlaceId(Long placeId) {
        return jpa.countByPlaceId(placeId);
    }

    @Override
    public boolean existsByUserIdAndPlaceId(Long userId, Long placeId) {
        return jpa.existsByUserIdAndPlaceId(userId, placeId);
    }
}
