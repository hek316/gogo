package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"kakao_id", "provider"}))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false)
    private String oauthId;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected User() {}

    public static User create(String oauthId, String nickname, String profileImageUrl, OAuthProvider provider) {
        User user = new User();
        user.oauthId = oauthId;
        user.nickname = nickname;
        user.profileImageUrl = profileImageUrl;
        user.provider = provider;
        user.createdAt = LocalDateTime.now();
        return user;
    }

    public static User reconstruct(Long id, String oauthId, String nickname, String profileImageUrl,
                                   OAuthProvider provider, LocalDateTime createdAt) {
        User user = new User();
        user.id = id;
        user.oauthId = oauthId;
        user.nickname = nickname;
        user.profileImageUrl = profileImageUrl;
        user.provider = provider;
        user.createdAt = createdAt;
        return user;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() { return id; }
    public String getOauthId() { return oauthId; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public OAuthProvider getProvider() { return provider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
