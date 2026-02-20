package com.gogo.domain.entity;

import java.time.LocalDateTime;

public class GroupPlace {

    private Long id;
    private Long groupId;
    private Long placeId;
    private String sharedBy;
    private LocalDateTime sharedAt;

    private GroupPlace() {}

    public static GroupPlace create(Long groupId, Long placeId, String sharedBy) {
        GroupPlace gp = new GroupPlace();
        gp.groupId = groupId;
        gp.placeId = placeId;
        gp.sharedBy = sharedBy;
        gp.sharedAt = LocalDateTime.now();
        return gp;
    }

    public static GroupPlace reconstruct(Long id, Long groupId, Long placeId, String sharedBy, LocalDateTime sharedAt) {
        GroupPlace gp = new GroupPlace();
        gp.id = id;
        gp.groupId = groupId;
        gp.placeId = placeId;
        gp.sharedBy = sharedBy;
        gp.sharedAt = sharedAt;
        return gp;
    }

    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public Long getPlaceId() { return placeId; }
    public String getSharedBy() { return sharedBy; }
    public LocalDateTime getSharedAt() { return sharedAt; }
}
