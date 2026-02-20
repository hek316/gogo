package com.gogo.domain.entity;

import java.time.LocalDateTime;

public class MeetingVote {

    private Long id;
    private Long meetingId;
    private Long placeId;
    private String voterName;
    private LocalDateTime votedAt;

    private MeetingVote() {}

    public static MeetingVote create(Long meetingId, Long placeId, String voterName) {
        MeetingVote v = new MeetingVote();
        v.meetingId = meetingId;
        v.placeId = placeId;
        v.voterName = voterName;
        v.votedAt = LocalDateTime.now();
        return v;
    }

    public static MeetingVote reconstruct(Long id, Long meetingId, Long placeId,
                                          String voterName, LocalDateTime votedAt) {
        MeetingVote v = new MeetingVote();
        v.id = id;
        v.meetingId = meetingId;
        v.placeId = placeId;
        v.voterName = voterName;
        v.votedAt = votedAt;
        return v;
    }

    public Long getId() { return id; }
    public Long getMeetingId() { return meetingId; }
    public Long getPlaceId() { return placeId; }
    public String getVoterName() { return voterName; }
    public LocalDateTime getVotedAt() { return votedAt; }
}
