package com.gogo.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "place_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "place_id"}))
public class PlaceLikeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PlaceLikeJpaEntity() {}

    public PlaceLikeJpaEntity(Long id, Long userId, Long placeId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.placeId = placeId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getPlaceId() { return placeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
