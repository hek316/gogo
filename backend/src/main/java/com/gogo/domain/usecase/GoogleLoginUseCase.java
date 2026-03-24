package com.gogo.domain.usecase;

import com.gogo.client.google.GoogleOAuthClient;
import com.gogo.domain.port.TokenProvider;
import com.gogo.db.entity.OAuthProvider;
import com.gogo.db.entity.RefreshToken;
import com.gogo.db.entity.User;
import com.gogo.db.repository.RefreshTokenRepository;
import com.gogo.db.repository.UserRepository;
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
    private final TokenProvider tokenProvider;

    public GoogleLoginUseCase(GoogleOAuthClient googleOAuthClient,
                              UserRepository userRepository,
                              RefreshTokenRepository refreshTokenRepository,
                              TokenProvider tokenProvider) {
        this.googleOAuthClient = googleOAuthClient;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
    }

    public KakaoLoginUseCase.TokenPair execute(String code) {
        GoogleOAuthClient.GoogleTokenResponse googleTokens = googleOAuthClient.exchangeCode(code);
        GoogleOAuthClient.GoogleUserInfo userInfo = googleOAuthClient.getUserInfo(googleTokens.access_token());

        User user = upsertUser(userInfo);

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getNickname());
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.create(
                rawRefreshToken, user.getId(),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        );
        refreshToken.setTokenHash(KakaoLoginUseCase.sha256(rawRefreshToken));
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
