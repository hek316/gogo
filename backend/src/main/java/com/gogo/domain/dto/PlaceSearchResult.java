package com.gogo.domain.dto;

public record PlaceSearchResult(
        String name,
        String address,
        String mapUrl,
        String category,
        String phone
) {}
