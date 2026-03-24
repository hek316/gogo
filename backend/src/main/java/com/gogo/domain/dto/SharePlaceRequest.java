package com.gogo.domain.dto;

import jakarta.validation.constraints.NotNull;

public record SharePlaceRequest(
        Long groupId,
        @NotNull Long placeId
) {}
