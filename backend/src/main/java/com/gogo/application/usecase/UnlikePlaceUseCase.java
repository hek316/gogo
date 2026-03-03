package com.gogo.application.usecase;

import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnlikePlaceUseCase {

    private final PlaceLikeRepository placeLikeRepository;
    private final SecurityContextHelper securityContextHelper;

    public UnlikePlaceUseCase(PlaceLikeRepository placeLikeRepository, SecurityContextHelper securityContextHelper) {
        this.placeLikeRepository = placeLikeRepository;
        this.securityContextHelper = securityContextHelper;
    }

    public void execute(Long placeId) {
        Long userId = securityContextHelper.currentUserId()
                .orElseThrow(() -> new IllegalStateException("인증 정보가 없습니다."));
        placeLikeRepository.delete(userId, placeId);
    }
}
