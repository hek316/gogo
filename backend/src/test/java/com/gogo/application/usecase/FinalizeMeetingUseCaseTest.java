package com.gogo.application.usecase;

import com.gogo.application.dto.FinalizeRequest;
import com.gogo.application.dto.MeetingResponse;
import com.gogo.domain.entity.Meeting;
import com.gogo.domain.entity.MeetingStatus;
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
class FinalizeMeetingUseCaseTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingVoteRepository meetingVoteRepository;

    @InjectMocks
    private FinalizeMeetingUseCase finalizeMeetingUseCase;

    @Test
    void 약속_확정_성공() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));
        given(meetingVoteRepository.findByMeetingId(1L)).willReturn(List.of());
        Meeting confirmed = Meeting.create(1L, "약속", List.of(1L, 2L));
        confirmed.confirm(1L);
        given(meetingRepository.save(any())).willReturn(confirmed);

        MeetingResponse response = finalizeMeetingUseCase.execute(1L, new FinalizeRequest(1L));

        assertThat(response.status()).isEqualTo(MeetingStatus.CONFIRMED);
        assertThat(response.confirmedPlaceId()).isEqualTo(1L);
    }

    @Test
    void 이미_확정된_약속_재확정_예외() {
        Meeting meeting = Meeting.create(1L, "약속", List.of(1L, 2L));
        meeting.confirm(1L);
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> finalizeMeetingUseCase.execute(1L, new FinalizeRequest(2L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("확정");
    }
}
