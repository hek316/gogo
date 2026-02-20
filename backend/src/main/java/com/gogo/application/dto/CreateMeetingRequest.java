package com.gogo.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateMeetingRequest(
        Long groupId,
        @NotBlank(message = "약속 제목은 필수입니다.") String title,
        @NotEmpty(message = "후보 장소는 최소 1개 이상이어야 합니다.") List<Long> candidatePlaceIds
) {}
