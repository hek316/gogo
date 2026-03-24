package com.gogo.domain.service;

import com.gogo.client.kakao.KakaoOAuthClient;
import com.gogo.client.google.GoogleOAuthClient;
import com.gogo.domain.port.AuthContext;
import com.gogo.db.entity.User;
import com.gogo.db.repository.RefreshTokenRepository;
import com.gogo.db.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthContext authContext;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;

    public AuthService(RefreshTokenRepository refreshTokenRepository,
                       UserRepository userRepository,
                       AuthContext authContext,
                       KakaoOAuthClient kakaoOAuthClient,
                       GoogleOAuthClient googleOAuthClient) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authContext = authContext;
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.googleOAuthClient = googleOAuthClient;
    }

    public String getKakaoAuthUrl() {
        return kakaoOAuthClient.buildAuthorizationUrl();
    }

    public String getGoogleAuthUrl() {
        return googleOAuthClient.buildAuthorizationUrl();
    }

    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
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
