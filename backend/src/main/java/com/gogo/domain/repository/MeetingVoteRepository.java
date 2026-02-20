package com.gogo.domain.repository;

import com.gogo.domain.entity.MeetingVote;

import java.util.List;
import java.util.Optional;

public interface MeetingVoteRepository {
    MeetingVote save(MeetingVote vote);
    Optional<MeetingVote> findByMeetingIdAndVoterName(Long meetingId, String voterName);
    List<MeetingVote> findByMeetingId(Long meetingId);
    void deleteById(Long id);
}
