package com.gogo.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {

    private Long id;
    private String name;
    private String inviteCode;
    private String createdBy;
    private List<GroupMember> members;
    private LocalDateTime createdAt;

    private Group() {}

    public static Group create(String name, String createdBy) {
        validate(name);
        Group group = new Group();
        group.name = name;
        group.inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        group.createdBy = createdBy;
        group.members = new ArrayList<>();
        group.createdAt = LocalDateTime.now();
        return group;
    }

    public static Group reconstruct(Long id, String name, String inviteCode,
                                    String createdBy, List<GroupMember> members, LocalDateTime createdAt) {
        Group group = new Group();
        group.id = id;
        group.name = name;
        group.inviteCode = inviteCode;
        group.createdBy = createdBy;
        group.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        group.createdAt = createdAt;
        return group;
    }

    public void addMember(String nickname) {
        members.add(new GroupMember(nickname));
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("그룹 이름은 필수입니다.");
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getInviteCode() { return inviteCode; }
    public String getCreatedBy() { return createdBy; }
    public List<GroupMember> getMembers() { return members; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
