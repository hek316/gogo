package com.gogo.application.usecase;

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
class VotePlaceUseCaseTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingVoteRepository meetingVoteRepository;

    @InjectMocks
    private VotePlaceUseCase votePlaceUseCase;

    @Test
    void 투표_성공() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));
        given(meetingVoteRepository.findByMeetingIdAndVoterName(any(), any())).willReturn(Optional.empty());
        MeetingVote vote = MeetingVote.create(1L, 1L, "홍길동");
        given(meetingVoteRepository.save(any())).willReturn(vote);
        given(meetingVoteRepository.findByMeetingId(any())).willReturn(List.of(vote));

        MeetingResponse response = votePlaceUseCase.execute(1L, new VoteRequest(1L, "홍길동"));

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

        MeetingResponse response = votePlaceUseCase.execute(1L, new VoteRequest(2L, "홍길동"));

        assertThat(response).isNotNull();
    }

    @Test
    void 확정된_약속에_투표_불가() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        meeting.confirm(1L);
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> votePlaceUseCase.execute(1L, new VoteRequest(1L, "홍길동")))
                .isInstanceOf(IllegalStateException.class);
    }
}
