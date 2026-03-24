package com.gogo.domain.service;

import com.gogo.domain.port.AuthContext;
import com.gogo.db.entity.PlaceLike;
import com.gogo.db.repository.PlaceLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlaceLikeService {

    private final PlaceLikeRepository placeLikeRepository;
    private final AuthContext authContext;

    public PlaceLikeService(PlaceLikeRepository placeLikeRepository, AuthContext authContext) {
        this.placeLikeRepository = placeLikeRepository;
        this.authContext = authContext;
    }

    public void like(Long placeId) {
        Long userId = authContext.requireUserId();
        if (!placeLikeRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            placeLikeRepository.save(PlaceLike.create(userId, placeId));
        }
    }

    public void unlike(Long placeId) {
        Long userId = authContext.requireUserId();
        placeLikeRepository.deleteByUserIdAndPlaceId(userId, placeId);
    }
}
