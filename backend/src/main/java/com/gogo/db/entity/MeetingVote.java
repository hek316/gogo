package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_votes")
public class MeetingVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long meetingId;

    @Column(nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private String voterName;

    @Column(nullable = false)
    private LocalDateTime votedAt;

    protected MeetingVote() {}

    public static MeetingVote create(Long meetingId, Long placeId, String voterName) {
        MeetingVote v = new MeetingVote();
        v.meetingId = meetingId;
        v.placeId = placeId;
        v.voterName = voterName;
        v.votedAt = LocalDateTime.now();
        return v;
    }

    public Long getId() { return id; }
    public Long getMeetingId() { return meetingId; }
    public Long getPlaceId() { return placeId; }
    public String getVoterName() { return voterName; }
    public LocalDateTime getVotedAt() { return votedAt; }
}
