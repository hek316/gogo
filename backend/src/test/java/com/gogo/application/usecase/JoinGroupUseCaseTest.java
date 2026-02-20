package com.gogo.application.usecase;

import com.gogo.application.dto.GroupResponse;
import com.gogo.application.dto.JoinGroupRequest;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JoinGroupUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private JoinGroupUseCase joinGroupUseCase;

    @Test
    void 유효한_초대코드로_그룹_참여() {
        Group group = Group.create("탐방대", "홍길동");
        given(groupRepository.findByInviteCode(group.getInviteCode())).willReturn(Optional.of(group));
        given(groupRepository.save(any())).willReturn(group);

        GroupResponse response = joinGroupUseCase.execute(new JoinGroupRequest(group.getInviteCode(), "김철수"));

        assertThat(response).isNotNull();
        assertThat(response.members()).hasSize(1);
    }

    @Test
    void 잘못된_초대코드_예외() {
        given(groupRepository.findByInviteCode("invalid0")).willReturn(Optional.empty());

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupRequest("invalid0", "김철수")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초대 코드");
    }
}
