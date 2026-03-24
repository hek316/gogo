package com.gogo.api;

import com.gogo.domain.dto.*;
import com.gogo.domain.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MeetingsController {

    private final MeetingService meetingService;

    public MeetingsController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/api/groups/{groupId}/meetings")
    public ResponseEntity<MeetingResponse> createMeeting(@PathVariable Long groupId,
                                                          @Valid @RequestBody CreateMeetingRequest request) {
        CreateMeetingRequest withGroupId = new CreateMeetingRequest(groupId, request.title(), request.candidatePlaceIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.createMeeting(withGroupId));
    }

    @GetMapping("/api/groups/{groupId}/meetings/{id}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long groupId,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getMeetingResult(id));
    }

    @PostMapping("/api/meetings/{id}/vote")
    public ResponseEntity<MeetingResponse> vote(@PathVariable Long id,
                                                 @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(meetingService.vote(id, request));
    }

    @PostMapping("/api/meetings/{id}/finalize")
    public ResponseEntity<MeetingResponse> finalize(@PathVariable Long id,
                                                     @Valid @RequestBody FinalizeRequest request) {
        return ResponseEntity.ok(meetingService.finalize(id, request));
    }
}
