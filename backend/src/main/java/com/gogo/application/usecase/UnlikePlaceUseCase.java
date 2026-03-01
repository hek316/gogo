package com.gogo.application.usecase;

import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.infrastructure.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnlikePlaceUseCase {

    private final PlaceLikeRepository placeLikeRepository;

    public UnlikePlaceUseCase(PlaceLikeRepository placeLikeRepository) {
        this.placeLikeRepository = placeLikeRepository;
    }

    public void execute(Long placeId) {
        Long userId = extractUserId();
        placeLikeRepository.delete(userId, placeId);
    }

    private Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user.userId();
        }
        throw new IllegalStateException("인증 정보가 없습니다.");
    }
}
