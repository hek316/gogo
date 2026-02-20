package com.gogo.application.usecase;

import com.gogo.application.dto.CreateMeetingRequest;
import com.gogo.application.dto.MeetingResponse;
import com.gogo.domain.entity.Meeting;
import com.gogo.domain.entity.MeetingStatus;
import com.gogo.domain.repository.MeetingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CreateMeetingUseCaseTest {

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private CreateMeetingUseCase createMeetingUseCase;

    @Test
    void 약속_생성_성공() {
        Meeting meeting = Meeting.create(1L, "이번 주말 약속", List.of(1L, 2L, 3L));
        given(meetingRepository.save(any())).willReturn(meeting);

        MeetingResponse response = createMeetingUseCase.execute(
                new CreateMeetingRequest(1L, "이번 주말 약속", List.of(1L, 2L, 3L)));

        assertThat(response.status()).isEqualTo(MeetingStatus.VOTING);
        assertThat(response.candidatePlaceIds()).hasSize(3);
    }

    @Test
    void 후보_장소_없이_생성시_예외() {
        assertThatThrownBy(() -> createMeetingUseCase.execute(
                new CreateMeetingRequest(1L, "약속", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("후보 장소");
    }
}
