package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    protected GroupMember() {}

    public GroupMember(String nickname) {
        this.nickname = nickname;
        this.joinedAt = LocalDateTime.now();
    }

    void setGroup(Group group) {
        this.group = group;
    }

    public Long getId() { return id; }
    public Group getGroup() { return group; }
    public String getNickname() { return nickname; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
