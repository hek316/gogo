package com.gogo.application.usecase;

import com.gogo.domain.entity.PlaceLike;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikePlaceUseCase {

    private final PlaceLikeRepository placeLikeRepository;
    private final SecurityContextHelper securityContextHelper;

    public LikePlaceUseCase(PlaceLikeRepository placeLikeRepository, SecurityContextHelper securityContextHelper) {
        this.placeLikeRepository = placeLikeRepository;
        this.securityContextHelper = securityContextHelper;
    }

    public void execute(Long placeId) {
        Long userId = securityContextHelper.currentUserId()
                .orElseThrow(() -> new IllegalStateException("인증 정보가 없습니다."));
        if (!placeLikeRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            placeLikeRepository.save(PlaceLike.create(userId, placeId));
        }
    }
}
