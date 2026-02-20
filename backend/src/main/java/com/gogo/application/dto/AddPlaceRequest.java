package com.gogo.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AddPlaceRequest(
        @NotBlank(message = "장소 이름은 필수입니다.") String name,
        String address,
        String category,
        String url,
        String note,
        @NotBlank(message = "작성자는 필수입니다.") String createdBy
) {}
