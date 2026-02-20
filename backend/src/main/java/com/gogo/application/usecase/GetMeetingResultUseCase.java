package com.gogo.application.usecase;

import com.gogo.application.dto.MeetingResponse;
import com.gogo.domain.entity.MeetingVote;
import com.gogo.domain.repository.MeetingRepository;
import com.gogo.domain.repository.MeetingVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetMeetingResultUseCase {

    private final MeetingRepository meetingRepository;
    private final MeetingVoteRepository meetingVoteRepository;

    public GetMeetingResultUseCase(MeetingRepository meetingRepository, MeetingVoteRepository meetingVoteRepository) {
        this.meetingRepository = meetingRepository;
        this.meetingVoteRepository = meetingVoteRepository;
    }

    public MeetingResponse execute(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .map(meeting -> {
                    List<MeetingVote> votes = meetingVoteRepository.findByMeetingId(meetingId);
                    return MeetingResponse.of(meeting, votes);
                })
                .orElseThrow(() -> new IllegalArgumentException("약속을 찾을 수 없습니다. id=" + meetingId));
    }
}
