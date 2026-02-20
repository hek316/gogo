package com.gogo.application.usecase;

import com.gogo.application.dto.CreateMeetingRequest;
import com.gogo.application.dto.MeetingResponse;
import com.gogo.domain.entity.Meeting;
import com.gogo.domain.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CreateMeetingUseCase {

    private final MeetingRepository meetingRepository;

    public CreateMeetingUseCase(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public MeetingResponse execute(CreateMeetingRequest request) {
        Meeting meeting = Meeting.create(request.groupId(), request.title(), request.candidatePlaceIds());
        Meeting saved = meetingRepository.save(meeting);
        return MeetingResponse.of(saved, List.of());
    }
}
