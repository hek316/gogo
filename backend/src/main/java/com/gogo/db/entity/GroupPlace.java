package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_places")
public class GroupPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private String sharedBy;

    @Column(nullable = false)
    private LocalDateTime sharedAt;

    protected GroupPlace() {}

    public static GroupPlace create(Long groupId, Long placeId, String sharedBy) {
        GroupPlace gp = new GroupPlace();
        gp.groupId = groupId;
        gp.placeId = placeId;
        gp.sharedBy = sharedBy;
        gp.sharedAt = LocalDateTime.now();
        return gp;
    }

    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public Long getPlaceId() { return placeId; }
    public String getSharedBy() { return sharedBy; }
    public LocalDateTime getSharedAt() { return sharedAt; }
}
