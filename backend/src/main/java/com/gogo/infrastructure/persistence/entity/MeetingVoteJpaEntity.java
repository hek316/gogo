package com.gogo.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_votes")
public class MeetingVoteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private String voterName;

    @Column(nullable = false)
    private LocalDateTime votedAt;

    protected MeetingVoteJpaEntity() {}

    public MeetingVoteJpaEntity(Long meetingId, Long placeId, String voterName, LocalDateTime votedAt) {
        this.meetingId = meetingId;
        this.placeId = placeId;
        this.voterName = voterName;
        this.votedAt = votedAt;
    }

    public Long getId() { return id; }
    public Long getMeetingId() { return meetingId; }
    public Long getPlaceId() { return placeId; }
    public String getVoterName() { return voterName; }
    public LocalDateTime getVotedAt() { return votedAt; }
}
