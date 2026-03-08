package com.gogo.application.service;

import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.User;
import com.gogo.domain.repository.RefreshTokenRepository;
import com.gogo.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthContext authContext;

    public AuthService(RefreshTokenRepository refreshTokenRepository,
                       UserRepository userRepository,
                       AuthContext authContext) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authContext = authContext;
    }

    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        Long userId = authContext.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new CurrentUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl() != null ? user.getProfileImageUrl() : ""
        );
    }

    public record CurrentUserResponse(Long id, String nickname, String profileImageUrl) {}
}
