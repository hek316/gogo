package com.gogo.application.dto;

import com.gogo.domain.entity.GroupMember;

import java.time.LocalDateTime;

public record GroupMemberResponse(Long id, String nickname, LocalDateTime joinedAt) {
    public static GroupMemberResponse from(GroupMember member) {
        return new GroupMemberResponse(member.getId(), member.getNickname(), member.getJoinedAt());
    }
}
