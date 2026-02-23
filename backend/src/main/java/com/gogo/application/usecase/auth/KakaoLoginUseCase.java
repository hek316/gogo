package com.gogo.application.usecase.auth;

import com.gogo.application.auth.JwtService;
import com.gogo.application.auth.KakaoOAuthClient;
import com.gogo.domain.entity.OAuthProvider;
import com.gogo.domain.entity.RefreshToken;
import com.gogo.domain.entity.User;
import com.gogo.domain.repository.RefreshTokenRepository;
import com.gogo.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class KakaoLoginUseCase {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public KakaoLoginUseCase(KakaoOAuthClient kakaoOAuthClient,
                             UserRepository userRepository,
                             RefreshTokenRepository refreshTokenRepository,
                             JwtService jwtService) {
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public TokenPair execute(String code) {
        KakaoOAuthClient.KakaoTokenResponse kakaoTokens = kakaoOAuthClient.exchangeCode(code);
        KakaoOAuthClient.KakaoUserInfo userInfo = kakaoOAuthClient.getUserInfo(kakaoTokens.accessToken());

        User user = upsertUser(userInfo);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getNickname());
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.create(
                rawRefreshToken,
                user.getId(),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        );
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, rawRefreshToken);
    }

    private User upsertUser(KakaoOAuthClient.KakaoUserInfo userInfo) {
        Optional<User> existing = userRepository.findByKakaoIdAndProvider(
                userInfo.id(), OAuthProvider.KAKAO);

        if (existing.isPresent()) {
            User user = existing.get();
            user.updateProfile(userInfo.nickname(), userInfo.profileImageUrl());
            return userRepository.save(user);
        }

        User newUser = User.create(userInfo.id(), userInfo.nickname(),
                userInfo.profileImageUrl(), OAuthProvider.KAKAO);
        return userRepository.save(newUser);
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
