package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.MeetingVote;
import com.gogo.domain.repository.MeetingVoteRepository;
import com.gogo.infrastructure.persistence.entity.MeetingVoteJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MeetingVoteRepositoryImpl implements MeetingVoteRepository {

    private final MeetingVoteJpaRepository meetingVoteJpaRepository;

    public MeetingVoteRepositoryImpl(MeetingVoteJpaRepository meetingVoteJpaRepository) {
        this.meetingVoteJpaRepository = meetingVoteJpaRepository;
    }

    @Override
    public MeetingVote save(MeetingVote vote) {
        MeetingVoteJpaEntity saved = meetingVoteJpaRepository.save(
                new MeetingVoteJpaEntity(vote.getMeetingId(), vote.getPlaceId(),
                        vote.getVoterName(), vote.getVotedAt()));
        return MeetingVote.reconstruct(saved.getId(), saved.getMeetingId(),
                saved.getPlaceId(), saved.getVoterName(), saved.getVotedAt());
    }

    @Override
    public Optional<MeetingVote> findByMeetingIdAndVoterName(Long meetingId, String voterName) {
        return meetingVoteJpaRepository.findByMeetingIdAndVoterName(meetingId, voterName)
                .map(e -> MeetingVote.reconstruct(e.getId(), e.getMeetingId(),
                        e.getPlaceId(), e.getVoterName(), e.getVotedAt()));
    }

    @Override
    public List<MeetingVote> findByMeetingId(Long meetingId) {
        return meetingVoteJpaRepository.findByMeetingId(meetingId).stream()
                .map(e -> MeetingVote.reconstruct(e.getId(), e.getMeetingId(),
                        e.getPlaceId(), e.getVoterName(), e.getVotedAt()))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        meetingVoteJpaRepository.deleteById(id);
    }
}
