package com.gogo.application.usecase;

import com.gogo.application.dto.CreateGroupRequest;
import com.gogo.application.dto.GroupResponse;
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
class CreateGroupUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private CreateGroupUseCase createGroupUseCase;

    @BeforeEach
    void setUp() {
        given(authContext.currentNickname()).willReturn(Optional.of("tester"));
    }

    @Test
    void createGroupGeneratesInviteCode() {
        Group group = Group.create("test-group", "tester");
        given(groupRepository.save(any())).willReturn(group);

        GroupResponse response = createGroupUseCase.execute(new CreateGroupRequest("test-group"));

        assertThat(response.inviteCode()).isNotNull();
        assertThat(response.inviteCode()).hasSize(8);
    }

    @Test
    void emptyGroupNameThrows() {
        assertThatThrownBy(() -> createGroupUseCase.execute(new CreateGroupRequest("")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
