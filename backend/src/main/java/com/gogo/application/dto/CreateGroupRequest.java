package com.gogo.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(
        @NotBlank(message = "그룹 이름은 필수입니다.") String name,
        @NotBlank(message = "작성자는 필수입니다.") String createdBy
) {}
