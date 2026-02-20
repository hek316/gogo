package com.gogo.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Meeting {

    private Long id;
    private Long groupId;
    private String title;
    private List<Long> candidatePlaceIds;
    private MeetingStatus status;
    private Long confirmedPlaceId;
    private LocalDateTime createdAt;

    private Meeting() {}

    public static Meeting create(Long groupId, String title, List<Long> candidatePlaceIds) {
        if (candidatePlaceIds == null || candidatePlaceIds.isEmpty()) {
            throw new IllegalArgumentException("후보 장소는 최소 1개 이상이어야 합니다.");
        }
        Meeting meeting = new Meeting();
        meeting.groupId = groupId;
        meeting.title = title;
        meeting.candidatePlaceIds = new ArrayList<>(candidatePlaceIds);
        meeting.status = MeetingStatus.VOTING;
        meeting.createdAt = LocalDateTime.now();
        return meeting;
    }

    public static Meeting reconstruct(Long id, Long groupId, String title, List<Long> candidatePlaceIds,
                                      MeetingStatus status, Long confirmedPlaceId, LocalDateTime createdAt) {
        Meeting meeting = new Meeting();
        meeting.id = id;
        meeting.groupId = groupId;
        meeting.title = title;
        meeting.candidatePlaceIds = candidatePlaceIds != null ? candidatePlaceIds : new ArrayList<>();
        meeting.status = status;
        meeting.confirmedPlaceId = confirmedPlaceId;
        meeting.createdAt = createdAt;
        return meeting;
    }

    public void confirm(Long placeId) {
        if (this.status == MeetingStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 약속입니다.");
        }
        this.confirmedPlaceId = placeId;
        this.status = MeetingStatus.CONFIRMED;
    }

    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public List<Long> getCandidatePlaceIds() { return candidatePlaceIds; }
    public MeetingStatus getStatus() { return status; }
    public Long getConfirmedPlaceId() { return confirmedPlaceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
