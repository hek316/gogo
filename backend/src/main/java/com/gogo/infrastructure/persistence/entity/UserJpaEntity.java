package com.gogo.infrastructure.persistence.entity;

import com.gogo.domain.entity.OAuthProvider;
import com.gogo.domain.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"kakao_id", "provider"}))
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false)
    private String kakaoId;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UserJpaEntity() {}

    public static UserJpaEntity from(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.id = user.getId();
        e.kakaoId = user.getKakaoId();
        e.nickname = user.getNickname();
        e.profileImageUrl = user.getProfileImageUrl();
        e.provider = user.getProvider();
        e.createdAt = user.getCreatedAt();
        return e;
    }

    public User toDomain() {
        return User.reconstruct(id, kakaoId, nickname, profileImageUrl, provider, createdAt);
    }

    public Long getId() { return id; }
    public String getKakaoId() { return kakaoId; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public OAuthProvider getProvider() { return provider; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
