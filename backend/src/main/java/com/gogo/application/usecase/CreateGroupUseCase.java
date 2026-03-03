package com.gogo.application.usecase;

import com.gogo.application.dto.CreateGroupRequest;
import com.gogo.application.dto.GroupResponse;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.application.port.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateGroupUseCase {

    private final GroupRepository groupRepository;
    private final AuthContext authContext;

    public CreateGroupUseCase(GroupRepository groupRepository, AuthContext authContext) {
        this.groupRepository = groupRepository;
        this.authContext = authContext;
    }

    public GroupResponse execute(CreateGroupRequest request) {
        String nickname = authContext.currentNickname().orElse("anonymous");
        Group group = Group.create(request.name(), nickname);
        return GroupResponse.from(groupRepository.save(group));
    }
}
