package com.gogo.application.usecase;

import com.gogo.application.dto.GroupResponse;
import com.gogo.application.dto.JoinGroupRequest;
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

    @BeforeEach
    void setUpSecurityContext() {
        AuthenticatedUser principal = new AuthenticatedUser(2L, "김철수");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 유효한_초대코드로_그룹_참여() {
        Group group = Group.create("탐방대", "홍길동");
        given(groupRepository.findByInviteCode(group.getInviteCode())).willReturn(Optional.of(group));
        given(groupRepository.save(any())).willReturn(group);

        GroupResponse response = joinGroupUseCase.execute(new JoinGroupRequest(group.getInviteCode()));

        assertThat(response).isNotNull();
        assertThat(response.members()).hasSize(1);
    }

    @Test
    void 잘못된_초대코드_예외() {
        given(groupRepository.findByInviteCode("invalid0")).willReturn(Optional.empty());

        assertThatThrownBy(() -> joinGroupUseCase.execute(new JoinGroupRequest("invalid0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초대 코드");
    }
}
