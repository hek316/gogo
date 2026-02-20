package com.gogo.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
public class GroupJpaEntity {

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

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private List<GroupMemberJpaEntity> members = new ArrayList<>();

    protected GroupJpaEntity() {}

    public GroupJpaEntity(Long id, String name, String inviteCode, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.inviteCode = inviteCode;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getInviteCode() { return inviteCode; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<GroupMemberJpaEntity> getMembers() { return members; }
}
