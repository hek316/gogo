package com.gogo.domain.repository;

import com.gogo.domain.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String tokenHash);
    void revokeAllByUserId(Long userId);
}
