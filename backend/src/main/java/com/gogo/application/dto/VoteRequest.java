package com.gogo.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(
        @NotNull(message = "장소 ID는 필수입니다.") Long placeId,
        @NotBlank(message = "투표자 이름은 필수입니다.") String voterName
) {}
