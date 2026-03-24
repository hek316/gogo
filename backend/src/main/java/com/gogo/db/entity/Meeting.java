package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meeting_candidates", joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "place_id")
    private List<Long> candidatePlaceIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    private Long confirmedPlaceId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Meeting() {}

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
