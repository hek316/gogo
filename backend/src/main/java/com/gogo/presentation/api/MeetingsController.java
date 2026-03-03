package com.gogo.presentation.api;

import com.gogo.application.dto.*;
import com.gogo.application.service.MeetingService;
import com.gogo.application.usecase.FinalizeMeetingUseCase;
import com.gogo.application.usecase.VotePlaceUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MeetingsController {

    private final MeetingService meetingService;
    private final VotePlaceUseCase votePlaceUseCase;
    private final FinalizeMeetingUseCase finalizeMeetingUseCase;

    public MeetingsController(MeetingService meetingService,
                               VotePlaceUseCase votePlaceUseCase,
                               FinalizeMeetingUseCase finalizeMeetingUseCase) {
        this.meetingService = meetingService;
        this.votePlaceUseCase = votePlaceUseCase;
        this.finalizeMeetingUseCase = finalizeMeetingUseCase;
    }

    // 약속 생성
    @PostMapping("/api/groups/{groupId}/meetings")
    public ResponseEntity<MeetingResponse> createMeeting(@PathVariable Long groupId,
                                                          @Valid @RequestBody CreateMeetingRequest request) {
        CreateMeetingRequest withGroupId = new CreateMeetingRequest(groupId, request.title(), request.candidatePlaceIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.createMeeting(withGroupId));
    }

    // 약속 상세 + 투표 현황
    @GetMapping("/api/groups/{groupId}/meetings/{id}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long groupId,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getMeetingResult(id));
    }

    // 투표
    @PostMapping("/api/meetings/{id}/vote")
    public ResponseEntity<MeetingResponse> vote(@PathVariable Long id,
                                                 @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(votePlaceUseCase.execute(id, request));
    }

    // 약속 확정
    @PostMapping("/api/meetings/{id}/finalize")
    public ResponseEntity<MeetingResponse> finalize(@PathVariable Long id,
                                                     @Valid @RequestBody FinalizeRequest request) {
        return ResponseEntity.ok(finalizeMeetingUseCase.execute(id, request));
    }
}
