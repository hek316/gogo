package com.gogo.db.repository;

import com.gogo.db.entity.MeetingVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingVoteRepository extends JpaRepository<MeetingVote, Long> {
    Optional<MeetingVote> findByMeetingIdAndVoterName(Long meetingId, String voterName);
    List<MeetingVote> findByMeetingId(Long meetingId);
}
