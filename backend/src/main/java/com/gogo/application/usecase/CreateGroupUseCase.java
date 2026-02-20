package com.gogo.application.usecase;

import com.gogo.application.dto.CreateGroupRequest;
import com.gogo.application.dto.GroupResponse;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateGroupUseCase {

    private final GroupRepository groupRepository;

    public CreateGroupUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupResponse execute(CreateGroupRequest request) {
        Group group = Group.create(request.name(), request.createdBy());
        return GroupResponse.from(groupRepository.save(group));
    }
}
