package com.gogo.domain.dto;

import com.gogo.db.entity.Place;
import com.gogo.db.entity.PlaceStatus;

import java.time.LocalDateTime;

public record PlaceResponse(
        Long id,
        String name,
        String address,
        String category,
        String url,
        String note,
        String imageUrl,
        PlaceStatus status,
        String createdBy,
        LocalDateTime createdAt,
        int likeCount,
        boolean isLiked
) {
    public static PlaceResponse from(Place place) {
        return from(place, 0, false);
    }

    public static PlaceResponse from(Place place, int likeCount, boolean isLiked) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getCategory(),
                place.getUrl(),
                place.getNote(),
                place.getImageUrl(),
                place.getStatus(),
                place.getCreatedBy(),
                place.getCreatedAt(),
                likeCount,
                isLiked
        );
    }
}
