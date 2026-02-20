package com.gogo.application.usecase;

import com.gogo.application.dto.GroupResponse;
import com.gogo.application.dto.JoinGroupRequest;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JoinGroupUseCase {

    private final GroupRepository groupRepository;

    public JoinGroupUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupResponse execute(JoinGroupRequest request) {
        Group group = groupRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));
        group.addMember(request.nickname());
        return GroupResponse.from(groupRepository.save(group));
    }
}
