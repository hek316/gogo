package com.gogo.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
public class GroupMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupJpaEntity group;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    protected GroupMemberJpaEntity() {}

    public GroupMemberJpaEntity(GroupJpaEntity group, String nickname, LocalDateTime joinedAt) {
        this.group = group;
        this.nickname = nickname;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public GroupJpaEntity getGroup() { return group; }
    public String getNickname() { return nickname; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
