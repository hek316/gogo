package com.gogo.domain.dto;

public record PlacePreviewResponse(
        String title,
        String imageUrl,
        String address,
        String description
) {}
