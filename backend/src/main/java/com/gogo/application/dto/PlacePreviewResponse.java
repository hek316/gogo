package com.gogo.application.dto;

public record PlacePreviewResponse(
        String title,
        String imageUrl,
        String address,
        String description
) {}
