package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String inviteCode;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members = new ArrayList<>();

    protected Group() {}

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

    public void addMember(String nickname) {
        GroupMember member = new GroupMember(nickname);
        member.setGroup(this);
        members.add(member);
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
