package com.gogo.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPlaceDocument(
        String place_name,
        String address_name,
        String road_address_name,
        String place_url,
        String category_name,
        String phone,
        String id
) {}
