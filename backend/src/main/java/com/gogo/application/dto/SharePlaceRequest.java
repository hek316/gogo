package com.gogo.application.dto;

import jakarta.validation.constraints.NotNull;

public record SharePlaceRequest(
        Long groupId,
        @NotNull Long placeId,
        String sharedBy
) {}
