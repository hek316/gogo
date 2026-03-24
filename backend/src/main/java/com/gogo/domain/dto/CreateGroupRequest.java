package com.gogo.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(
        @NotBlank(message = "그룹 이름은 필수입니다.") String name
) {}
