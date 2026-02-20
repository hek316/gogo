package com.gogo.application.usecase;

import com.gogo.application.dto.FinalizeRequest;
import com.gogo.application.dto.MeetingResponse;
import com.gogo.domain.entity.Meeting;
import com.gogo.domain.entity.MeetingVote;
import com.gogo.domain.repository.MeetingRepository;
import com.gogo.domain.repository.MeetingVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FinalizeMeetingUseCase {

    private final MeetingRepository meetingRepository;
    private final MeetingVoteRepository meetingVoteRepository;

    public FinalizeMeetingUseCase(MeetingRepository meetingRepository, MeetingVoteRepository meetingVoteRepository) {
        this.meetingRepository = meetingRepository;
        this.meetingVoteRepository = meetingVoteRepository;
    }

    public MeetingResponse execute(Long meetingId, FinalizeRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("약속을 찾을 수 없습니다. id=" + meetingId));
        meeting.confirm(request.confirmedPlaceId());
        Meeting saved = meetingRepository.save(meeting);
        List<MeetingVote> votes = meetingVoteRepository.findByMeetingId(meetingId);
        return MeetingResponse.of(saved, votes);
    }
}
