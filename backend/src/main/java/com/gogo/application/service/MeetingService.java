package com.gogo.application.service;

import com.gogo.application.dto.CreateMeetingRequest;
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
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingVoteRepository meetingVoteRepository;

    public MeetingService(MeetingRepository meetingRepository, MeetingVoteRepository meetingVoteRepository) {
        this.meetingRepository = meetingRepository;
        this.meetingVoteRepository = meetingVoteRepository;
    }

    public MeetingResponse createMeeting(CreateMeetingRequest request) {
        Meeting meeting = Meeting.create(request.groupId(), request.title(), request.candidatePlaceIds());
        Meeting saved = meetingRepository.save(meeting);
        return MeetingResponse.of(saved, List.of());
    }

    @Transactional(readOnly = true)
    public MeetingResponse getMeetingResult(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .map(meeting -> {
                    List<MeetingVote> votes = meetingVoteRepository.findByMeetingId(meetingId);
                    return MeetingResponse.of(meeting, votes);
                })
                .orElseThrow(() -> new IllegalArgumentException("약속을 찾을 수 없습니다. id=" + meetingId));
    }
}
