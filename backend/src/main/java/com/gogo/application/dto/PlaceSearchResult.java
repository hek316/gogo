package com.gogo.application.dto;

public record PlaceSearchResult(
        String name,
        String address,
        String mapUrl,
        String category,
        String phone
) {}
