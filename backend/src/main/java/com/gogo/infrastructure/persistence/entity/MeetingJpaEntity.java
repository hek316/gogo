package com.gogo.infrastructure.persistence.entity;

import com.gogo.domain.entity.MeetingStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
public class MeetingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @Column(name = "confirmed_place_id")
    private Long confirmedPlaceId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "meeting", fetch = FetchType.EAGER)
    private List<MeetingCandidateJpaEntity> candidates = new ArrayList<>();

    protected MeetingJpaEntity() {}

    public MeetingJpaEntity(Long id, Long groupId, String title, MeetingStatus status,
                             Long confirmedPlaceId, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.status = status;
        this.confirmedPlaceId = confirmedPlaceId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public MeetingStatus getStatus() { return status; }
    public Long getConfirmedPlaceId() { return confirmedPlaceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<MeetingCandidateJpaEntity> getCandidates() { return candidates; }
}
