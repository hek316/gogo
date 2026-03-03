package com.gogo.application.service;

import com.gogo.application.dto.CreateMeetingRequest;
import com.gogo.application.dto.FinalizeRequest;
import com.gogo.application.dto.MeetingResponse;
import com.gogo.application.dto.VoteRequest;
import com.gogo.domain.entity.Meeting;
import com.gogo.domain.entity.MeetingStatus;
import com.gogo.domain.entity.MeetingVote;
import com.gogo.domain.repository.MeetingRepository;
import com.gogo.domain.repository.MeetingVoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock MeetingRepository meetingRepository;
    @Mock MeetingVoteRepository meetingVoteRepository;
    @InjectMocks MeetingService meetingService;

    @Test
    void 약속_생성_성공() {
        Meeting meeting = Meeting.create(1L, "이번 주말 약속", List.of(1L, 2L, 3L));
        given(meetingRepository.save(any())).willReturn(meeting);

        MeetingResponse response = meetingService.createMeeting(
                new CreateMeetingRequest(1L, "이번 주말 약속", List.of(1L, 2L, 3L)));

        assertThat(response.status()).isEqualTo(MeetingStatus.VOTING);
        assertThat(response.candidatePlaceIds()).hasSize(3);
    }

    @Test
    void 후보_장소_없이_생성시_예외() {
        assertThatThrownBy(() -> meetingService.createMeeting(
                new CreateMeetingRequest(1L, "약속", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("후보 장소");
    }

    @Test
    void 투표_성공() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));
        given(meetingVoteRepository.findByMeetingIdAndVoterName(any(), any())).willReturn(Optional.empty());
        MeetingVote vote = MeetingVote.create(1L, 1L, "홍길동");
        given(meetingVoteRepository.save(any())).willReturn(vote);
        given(meetingVoteRepository.findByMeetingId(any())).willReturn(List.of(vote));

        MeetingResponse response = meetingService.vote(1L, new VoteRequest(1L, "홍길동"));

        assertThat(response).isNotNull();
    }

    @Test
    void 재투표_시_이전_투표_변경() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));
        MeetingVote existing = MeetingVote.create(1L, 1L, "홍길동");
        given(meetingVoteRepository.findByMeetingIdAndVoterName(1L, "홍길동")).willReturn(Optional.of(existing));
        MeetingVote updated = MeetingVote.create(1L, 2L, "홍길동");
        given(meetingVoteRepository.save(any())).willReturn(updated);
        given(meetingVoteRepository.findByMeetingId(any())).willReturn(List.of(updated));

        MeetingResponse response = meetingService.vote(1L, new VoteRequest(2L, "홍길동"));

        assertThat(response).isNotNull();
    }

    @Test
    void 확정된_약속에_투표_불가() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        meeting.confirm(1L);
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> meetingService.vote(1L, new VoteRequest(1L, "홍길동")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 약속_확정_성공() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));
        given(meetingVoteRepository.findByMeetingId(1L)).willReturn(List.of());
        Meeting confirmed = Meeting.create(1L, "약속", List.of(1L, 2L));
        confirmed.confirm(1L);
        given(meetingRepository.save(any())).willReturn(confirmed);

        MeetingResponse response = meetingService.finalize(1L, new FinalizeRequest(1L));

        assertThat(response.status()).isEqualTo(MeetingStatus.CONFIRMED);
        assertThat(response.confirmedPlaceId()).isEqualTo(1L);
    }

    @Test
    void 이미_확정된_약속_재확정_예외() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        meeting.confirm(1L);
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> meetingService.finalize(1L, new FinalizeRequest(2L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("확정");
    }
}
