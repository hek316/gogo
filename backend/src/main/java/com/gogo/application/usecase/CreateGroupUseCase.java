package com.gogo.application.usecase;

import com.gogo.application.dto.CreateGroupRequest;
import com.gogo.application.dto.GroupResponse;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateGroupUseCase {

    private final GroupRepository groupRepository;
    private final SecurityContextHelper securityContextHelper;

    public CreateGroupUseCase(GroupRepository groupRepository, SecurityContextHelper securityContextHelper) {
        this.groupRepository = groupRepository;
        this.securityContextHelper = securityContextHelper;
    }

    public GroupResponse execute(CreateGroupRequest request) {
        String nickname = securityContextHelper.currentNickname().orElse("anonymous");
        Group group = Group.create(request.name(), nickname);
        return GroupResponse.from(groupRepository.save(group));
    }
}
