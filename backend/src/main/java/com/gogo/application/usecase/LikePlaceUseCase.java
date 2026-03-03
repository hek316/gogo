package com.gogo.application.usecase;

import com.gogo.domain.entity.PlaceLike;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.application.port.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikePlaceUseCase {

    private final PlaceLikeRepository placeLikeRepository;
    private final AuthContext authContext;

    public LikePlaceUseCase(PlaceLikeRepository placeLikeRepository, AuthContext authContext) {
        this.placeLikeRepository = placeLikeRepository;
        this.authContext = authContext;
    }

    public void execute(Long placeId) {
        Long userId = authContext.currentUserId()
                .orElseThrow(() -> new IllegalStateException("인증 정보가 없습니다."));
        if (!placeLikeRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            placeLikeRepository.save(PlaceLike.create(userId, placeId));
        }
    }
}
