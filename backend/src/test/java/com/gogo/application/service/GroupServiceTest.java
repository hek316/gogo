package com.gogo.application.service;

import com.gogo.application.dto.CreateGroupRequest;
import com.gogo.application.dto.GroupResponse;
import com.gogo.application.dto.JoinGroupRequest;
import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupPlaceRepository;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.domain.repository.PlaceRepository;
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
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupPlaceRepository groupPlaceRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        given(authContext.currentNickname()).willReturn(Optional.of("tester"));
    }

    @Test
    void createGroupGeneratesInviteCode() {
        Group group = Group.create("test-group", "tester");
        given(groupRepository.save(any())).willReturn(group);

        GroupResponse response = groupService.createGroup(new CreateGroupRequest("test-group"));

        assertThat(response.inviteCode()).isNotNull();
        assertThat(response.inviteCode()).hasSize(8);
    }

    @Test
    void emptyGroupNameThrows() {
        assertThatThrownBy(() -> groupService.createGroup(new CreateGroupRequest("")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void joinWithValidInviteCode() {
        given(authContext.currentNickname()).willReturn(Optional.of("joiner"));
        Group group = Group.create("test-group", "owner");
        given(groupRepository.findByInviteCode(group.getInviteCode())).willReturn(Optional.of(group));
        given(groupRepository.save(any())).willReturn(group);

        GroupResponse response = groupService.joinGroup(new JoinGroupRequest(group.getInviteCode()));

        assertThat(response).isNotNull();
        assertThat(response.members()).hasSize(1);
    }

    @Test
    void invalidInviteCodeThrows() {
        given(groupRepository.findByInviteCode("invalid0")).willReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.joinGroup(new JoinGroupRequest("invalid0")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
