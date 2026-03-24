package com.gogo.domain.usecase;

import com.gogo.client.kakao.KakaoOAuthClient;
import com.gogo.domain.port.TokenProvider;
import com.gogo.db.entity.OAuthProvider;
import com.gogo.db.entity.RefreshToken;
import com.gogo.db.entity.User;
import com.gogo.db.repository.RefreshTokenRepository;
import com.gogo.db.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class KakaoLoginUseCase {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public KakaoLoginUseCase(KakaoOAuthClient kakaoOAuthClient,
                             UserRepository userRepository,
                             RefreshTokenRepository refreshTokenRepository,
                             TokenProvider tokenProvider) {
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
    }

    public TokenPair execute(String code) {
        KakaoOAuthClient.KakaoTokenResponse kakaoTokens = kakaoOAuthClient.exchangeCode(code);
        KakaoOAuthClient.KakaoUserInfo userInfo = kakaoOAuthClient.getUserInfo(kakaoTokens.access_token());

        User user = upsertUser(userInfo);

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getNickname());
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.create(
                rawRefreshToken, user.getId(),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        );
        refreshToken.setTokenHash(sha256(rawRefreshToken));
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, rawRefreshToken);
    }

    private User upsertUser(KakaoOAuthClient.KakaoUserInfo userInfo) {
        Optional<User> existing = userRepository.findByOauthIdAndProvider(
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

    static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
