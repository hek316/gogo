package com.gogo.application.usecase;

import com.gogo.application.dto.GroupResponse;
import com.gogo.application.dto.JoinGroupRequest;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JoinGroupUseCase {

    private final GroupRepository groupRepository;
    private final SecurityContextHelper securityContextHelper;

    public JoinGroupUseCase(GroupRepository groupRepository, SecurityContextHelper securityContextHelper) {
        this.groupRepository = groupRepository;
        this.securityContextHelper = securityContextHelper;
    }

    public GroupResponse execute(JoinGroupRequest request) {
        String nickname = securityContextHelper.currentNickname().orElse("anonymous");
        Group group = groupRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));
        group.addMember(nickname);
        return GroupResponse.from(groupRepository.save(group));
    }
}
