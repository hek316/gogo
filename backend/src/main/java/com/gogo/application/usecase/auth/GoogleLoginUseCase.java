package com.gogo.application.usecase.auth;

import com.gogo.application.auth.GoogleOAuthClient;
import com.gogo.application.auth.JwtService;
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
public class GoogleLoginUseCase {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public GoogleLoginUseCase(GoogleOAuthClient googleOAuthClient,
                              UserRepository userRepository,
                              RefreshTokenRepository refreshTokenRepository,
                              JwtService jwtService) {
        this.googleOAuthClient = googleOAuthClient;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public KakaoLoginUseCase.TokenPair execute(String code) {
        GoogleOAuthClient.GoogleTokenResponse googleTokens = googleOAuthClient.exchangeCode(code);
        GoogleOAuthClient.GoogleUserInfo userInfo = googleOAuthClient.getUserInfo(googleTokens.access_token());

        User user = upsertUser(userInfo);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getNickname());
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.create(
                rawRefreshToken,
                user.getId(),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        );
        refreshTokenRepository.save(refreshToken);

        return new KakaoLoginUseCase.TokenPair(accessToken, rawRefreshToken);
    }

    private User upsertUser(GoogleOAuthClient.GoogleUserInfo userInfo) {
        Optional<User> existing = userRepository.findByOauthIdAndProvider(
                userInfo.id(), OAuthProvider.GOOGLE);

        if (existing.isPresent()) {
            User user = existing.get();
            user.updateProfile(userInfo.nickname(), userInfo.profileImageUrl());
            return userRepository.save(user);
        }

        User newUser = User.create(userInfo.id(), userInfo.nickname(),
                userInfo.profileImageUrl(), OAuthProvider.GOOGLE);
        return userRepository.save(newUser);
    }
}
