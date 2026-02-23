package com.gogo.domain.entity;

import java.time.LocalDateTime;

public class RefreshToken {

    private Long id;
    private String token;
    private Long userId;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;

    private RefreshToken() {}

    public static RefreshToken create(String token, Long userId, LocalDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.token = token;
        rt.userId = userId;
        rt.expiresAt = expiresAt;
        rt.createdAt = LocalDateTime.now();
        return rt;
    }

    public static RefreshToken reconstruct(Long id, String token, Long userId,
                                           LocalDateTime expiresAt, LocalDateTime revokedAt,
                                           LocalDateTime createdAt) {
        RefreshToken rt = new RefreshToken();
        rt.id = id;
        rt.token = token;
        rt.userId = userId;
        rt.expiresAt = expiresAt;
        rt.revokedAt = revokedAt;
        rt.createdAt = createdAt;
        return rt;
    }

    public boolean isValid() {
        return revokedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
