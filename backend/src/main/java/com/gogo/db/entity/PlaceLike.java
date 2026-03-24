package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "place_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "place_id"}))
public class PlaceLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PlaceLike() {}

    public static PlaceLike create(Long userId, Long placeId) {
        PlaceLike like = new PlaceLike();
        like.userId = userId;
        like.placeId = placeId;
        like.createdAt = LocalDateTime.now();
        return like;
    }

    public static PlaceLike reconstruct(Long id, Long userId, Long placeId, LocalDateTime createdAt) {
        PlaceLike like = new PlaceLike();
        like.id = id;
        like.userId = userId;
        like.placeId = placeId;
        like.createdAt = createdAt;
        return like;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getPlaceId() { return placeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
