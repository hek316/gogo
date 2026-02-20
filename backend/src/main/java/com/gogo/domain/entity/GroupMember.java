package com.gogo.domain.entity;

import java.time.LocalDateTime;

public class GroupMember {

    private Long id;
    private Long groupId;
    private String nickname;
    private LocalDateTime joinedAt;

    public GroupMember(String nickname) {
        this.nickname = nickname;
        this.joinedAt = LocalDateTime.now();
    }

    public GroupMember(Long id, Long groupId, String nickname, LocalDateTime joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.nickname = nickname;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public String getNickname() { return nickname; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
