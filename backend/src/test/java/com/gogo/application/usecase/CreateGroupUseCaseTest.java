package com.gogo.application.usecase;

import com.gogo.application.dto.CreateGroupRequest;
import com.gogo.application.dto.GroupResponse;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.infrastructure.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CreateGroupUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private CreateGroupUseCase createGroupUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        AuthenticatedUser principal = new AuthenticatedUser(1L, "홍길동");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 그룹_생성_시_초대코드_자동생성() {
        Group group = Group.create("성수동 탐방대", "홍길동");
        given(groupRepository.save(any())).willReturn(group);

        GroupResponse response = createGroupUseCase.execute(new CreateGroupRequest("성수동 탐방대"));

        assertThat(response.inviteCode()).isNotNull();
        assertThat(response.inviteCode()).hasSize(8);
    }

    @Test
    void 그룹_이름_빈문자열_예외() {
        assertThatThrownBy(() -> createGroupUseCase.execute(new CreateGroupRequest("")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("그룹 이름");
    }
}
