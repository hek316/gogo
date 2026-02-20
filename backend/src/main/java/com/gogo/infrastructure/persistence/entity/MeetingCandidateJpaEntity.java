package com.gogo.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "meeting_candidates")
public class MeetingCandidateJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingJpaEntity meeting;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    protected MeetingCandidateJpaEntity() {}

    public MeetingCandidateJpaEntity(MeetingJpaEntity meeting, Long placeId) {
        this.meeting = meeting;
        this.placeId = placeId;
    }

    public Long getId() { return id; }
    public MeetingJpaEntity getMeeting() { return meeting; }
    public Long getPlaceId() { return placeId; }
}
