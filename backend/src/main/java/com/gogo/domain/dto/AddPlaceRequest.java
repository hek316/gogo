package com.gogo.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record AddPlaceRequest(
        @NotBlank(message = "장소 이름은 필수입니다.") String name,
        String address,
        String category,
        String url,
        String note,
        String imageUrl
) {}
