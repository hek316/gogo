package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

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

    @Transient
    private String plainToken;

    protected RefreshToken() {}

    public static RefreshToken create(String plainToken, Long userId, LocalDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.plainToken = plainToken;
        rt.userId = userId;
        rt.expiresAt = expiresAt;
        rt.createdAt = LocalDateTime.now();
        return rt;
    }

    public boolean isValid() {
        return revokedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getPlainToken() { return plainToken; }
    public void setPlainToken(String plainToken) { this.plainToken = plainToken; }
    public Long getUserId() { return userId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
