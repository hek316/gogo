package com.gogo.domain.dto;

import jakarta.validation.constraints.NotNull;

public record FinalizeRequest(
        @NotNull(message = "확정 장소 ID는 필수입니다.") Long confirmedPlaceId
) {}
