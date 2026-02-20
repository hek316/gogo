package com.gogo.application.dto;

import com.gogo.domain.entity.Group;

import java.time.LocalDateTime;
import java.util.List;

public record GroupResponse(
        Long id,
        String name,
        String inviteCode,
        String createdBy,
        List<GroupMemberResponse> members,
        LocalDateTime createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getInviteCode(),
                group.getCreatedBy(),
                group.getMembers().stream().map(GroupMemberResponse::from).toList(),
                group.getCreatedAt()
        );
    }
}
