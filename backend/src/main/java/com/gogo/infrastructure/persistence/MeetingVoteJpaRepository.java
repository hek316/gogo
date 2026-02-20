package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.MeetingVoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingVoteJpaRepository extends JpaRepository<MeetingVoteJpaEntity, Long> {
    Optional<MeetingVoteJpaEntity> findByMeetingIdAndVoterName(Long meetingId, String voterName);
    List<MeetingVoteJpaEntity> findByMeetingId(Long meetingId);
}
