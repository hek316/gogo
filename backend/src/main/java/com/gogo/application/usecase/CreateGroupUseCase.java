package com.gogo.application.usecase;

import com.gogo.application.dto.CreateGroupRequest;
import com.gogo.application.dto.GroupResponse;
import com.gogo.domain.entity.Group;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.infrastructure.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String nickname = extractNickname();
        Group group = Group.create(request.name(), nickname);
        return GroupResponse.from(groupRepository.save(group));
    }

    private String extractNickname() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user.nickname();
        }
        return "anonymous";
    }
}
