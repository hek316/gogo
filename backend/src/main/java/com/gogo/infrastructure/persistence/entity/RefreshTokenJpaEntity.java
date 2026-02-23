package com.gogo.infrastructure.persistence.entity;

import com.gogo.domain.entity.RefreshToken;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected RefreshTokenJpaEntity() {}

    public static RefreshTokenJpaEntity from(RefreshToken rt, String tokenHash) {
        RefreshTokenJpaEntity e = new RefreshTokenJpaEntity();
        e.id = rt.getId();
        e.tokenHash = tokenHash;
        e.userId = rt.getUserId();
        e.expiresAt = rt.getExpiresAt();
        e.revokedAt = rt.getRevokedAt();
        e.createdAt = rt.getCreatedAt();
        return e;
    }

    public RefreshToken toDomain(String plainToken) {
        return RefreshToken.reconstruct(id, plainToken, userId, expiresAt, revokedAt, createdAt);
    }

    public Long getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public Long getUserId() { return userId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
}
