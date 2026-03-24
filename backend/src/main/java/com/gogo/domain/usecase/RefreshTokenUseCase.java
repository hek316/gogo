package com.gogo.domain.usecase;

import com.gogo.domain.port.TokenProvider;
import com.gogo.db.entity.RefreshToken;
import com.gogo.db.entity.User;
import com.gogo.db.repository.RefreshTokenRepository;
import com.gogo.db.repository.UserRepository;
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
    private final TokenProvider tokenProvider;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               TokenProvider tokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public KakaoLoginUseCase.TokenPair execute(String rawRefreshToken) {
        String hash = KakaoLoginUseCase.sha256(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 refresh token입니다."));

        if (!token.isValid()) {
            if (token.getRevokedAt() != null) {
                refreshTokenRepository.revokeAllByUserId(token.getUserId(), LocalDateTime.now());
            }
            throw new IllegalArgumentException("만료되었거나 폐기된 refresh token입니다.");
        }

        token.revoke();
        refreshTokenRepository.save(token);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getNickname());
        String newRawRefreshToken = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = RefreshToken.create(
                newRawRefreshToken, user.getId(),
                LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        );
        newRefreshToken.setTokenHash(KakaoLoginUseCase.sha256(newRawRefreshToken));
        refreshTokenRepository.save(newRefreshToken);

        return new KakaoLoginUseCase.TokenPair(newAccessToken, newRawRefreshToken);
    }
}
