package com.gogo.domain.entity;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String oauthId;
    private String nickname;
    private String profileImageUrl;
    private OAuthProvider provider;
    private LocalDateTime createdAt;

    private User() {}

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
