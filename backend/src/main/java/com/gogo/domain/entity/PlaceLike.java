package com.gogo.domain.entity;

import java.time.LocalDateTime;

public class PlaceLike {

    private Long id;
    private Long userId;
    private Long placeId;
    private LocalDateTime createdAt;

    private PlaceLike() {}

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
