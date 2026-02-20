package com.gogo.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_places")
public class GroupPlaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private String sharedBy;

    @Column(nullable = false)
    private LocalDateTime sharedAt;

    protected GroupPlaceJpaEntity() {}

    public GroupPlaceJpaEntity(Long groupId, Long placeId, String sharedBy, LocalDateTime sharedAt) {
        this.groupId = groupId;
        this.placeId = placeId;
        this.sharedBy = sharedBy;
        this.sharedAt = sharedAt;
    }

    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public Long getPlaceId() { return placeId; }
    public String getSharedBy() { return sharedBy; }
    public LocalDateTime getSharedAt() { return sharedAt; }
}
