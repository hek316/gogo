package com.gogo.application.dto;

import com.gogo.domain.entity.GroupPlace;

import java.time.LocalDateTime;

public record GroupPlaceResponse(
        Long id,
        Long groupId,
        PlaceResponse place,
        String sharedBy,
        LocalDateTime sharedAt
) {
    public static GroupPlaceResponse of(GroupPlace groupPlace, PlaceResponse place) {
        return new GroupPlaceResponse(
                groupPlace.getId(),
                groupPlace.getGroupId(),
                place,
                groupPlace.getSharedBy(),
                groupPlace.getSharedAt()
        );
    }
}
