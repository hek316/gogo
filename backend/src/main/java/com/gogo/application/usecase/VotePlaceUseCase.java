package com.gogo.application.usecase;

import com.gogo.application.dto.MeetingResponse;
import com.gogo.application.dto.VoteRequest;
import com.gogo.domain.entity.Meeting;
import com.gogo.domain.entity.MeetingStatus;
import com.gogo.domain.entity.MeetingVote;
import com.gogo.domain.repository.MeetingRepository;
import com.gogo.domain.repository.MeetingVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VotePlaceUseCase {

    private final MeetingRepository meetingRepository;
    private final MeetingVoteRepository meetingVoteRepository;

    public VotePlaceUseCase(MeetingRepository meetingRepository, MeetingVoteRepository meetingVoteRepository) {
        this.meetingRepository = meetingRepository;
        this.meetingVoteRepository = meetingVoteRepository;
    }

    public MeetingResponse execute(Long meetingId, VoteRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속을 찾을 수 없습니다. id=" + meetingId));

        if (meeting.getStatus() == MeetingStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 약속에는 투표할 수 없습니다.");
        }

        // 재투표 처리: 기존 투표 삭제 후 새로 저장
        Optional<MeetingVote> existing = meetingVoteRepository
                .findByMeetingIdAndVoterName(meetingId, request.voterName());
        existing.ifPresent(v -> meetingVoteRepository.deleteById(v.getId()));

        meetingVoteRepository.save(MeetingVote.create(meetingId, request.placeId(), request.voterName()));

        List<MeetingVote> votes = meetingVoteRepository.findByMeetingId(meetingId);
        return MeetingResponse.of(meeting, votes);
    }
}
