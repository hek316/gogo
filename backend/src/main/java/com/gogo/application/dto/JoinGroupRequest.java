package com.gogo.application.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGroupRequest(
        @NotBlank(message = "초대 코드는 필수입니다.") String inviteCode,
        @NotBlank(message = "닉네임은 필수입니다.") String nickname
) {}
