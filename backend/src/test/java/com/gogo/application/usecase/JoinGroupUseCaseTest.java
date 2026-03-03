package com.gogo.application.usecase;

import com.gogo.application.dto.GroupResponse;
import com.gogo.application.dto.JoinGroupRequest;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.application.port.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JoinGroupUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private JoinGroupUseCase joinGroupUseCase;

    @BeforeEach
    void setUp() {
        given(authContext.currentNickname()).willReturn(Optional.of("joiner"));
    }

    @Test
    void joinWithValidInviteCode() {
        Group group = Group.create("test-group", "owner");
        given(groupRepository.findByInviteCode(group.getInviteCode())).willReturn(Optional.of(group));
        given(groupRepository.save(any())).willReturn(group);

        GroupResponse response = joinGroupUseCase.execute(new JoinGroupRequest(group.getInviteCode()));

        assertThat(response).isNotNull();
        assertThat(response.members()).hasSize(1);
    }

    @Test
    void invalidInviteCodeThrows() {
        given(groupRepository.findByInviteCode("invalid0")).willReturn(Optional.empty());

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupRequest("invalid0")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
