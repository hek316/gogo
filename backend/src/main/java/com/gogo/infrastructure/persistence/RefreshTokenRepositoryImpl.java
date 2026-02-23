package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.RefreshToken;
import com.gogo.domain.repository.RefreshTokenRepository;
import com.gogo.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        String hash = sha256(refreshToken.getToken());
        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.from(refreshToken, hash);
        RefreshTokenJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain(refreshToken.getToken());
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        String hash = sha256(token);
        return jpaRepository.findByTokenHash(hash)
                .map(e -> e.toDomain(token));
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Long userId) {
        jpaRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
