package com.gogo.application.dto;

import com.gogo.domain.entity.Place;
import com.gogo.domain.entity.PlaceStatus;

import java.time.LocalDateTime;

public record PlaceResponse(
        Long id,
        String name,
        String address,
        String category,
        String url,
        String note,
        PlaceStatus status,
        String createdBy,
        LocalDateTime createdAt
) {
    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getCategory(),
                place.getUrl(),
                place.getNote(),
                place.getStatus(),
                place.getCreatedBy(),
                place.getCreatedAt()
        );
    }
}
