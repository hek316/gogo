package com.gogo.application.usecase.auth;

import com.gogo.application.auth.JwtService;
import com.gogo.domain.entity.RefreshToken;
import com.gogo.domain.entity.User;
import com.gogo.domain.repository.RefreshTokenRepository;
import com.gogo.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenUseCase {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public KakaoLoginUseCase.TokenPair execute(String rawRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 refresh token입니다."));

        if (!token.isValid()) {
            // Stolen token detection: revoke all tokens for this user
            if (token.getRevokedAt() != null) {
                refreshTokenRepository.revokeAllByUserId(token.getUserId());
            }
            throw new IllegalArgumentException("만료되었거나 폐기된 refresh token입니다.");
        }

        // Revoke the used token (rotation)
        token.revoke();
        refreshTokenRepository.save(token);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getNickname());
        String newRawRefreshToken = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = RefreshToken.create(
                newRawRefreshToken,
                user.getId(),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        );
        refreshTokenRepository.save(newRefreshToken);

        return new KakaoLoginUseCase.TokenPair(newAccessToken, newRawRefreshToken);
    }
}
