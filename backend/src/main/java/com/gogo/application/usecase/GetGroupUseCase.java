package com.gogo.application.usecase;

import com.gogo.application.dto.GroupResponse;
import com.gogo.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetGroupUseCase {

    private final GroupRepository groupRepository;

    public GetGroupUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupResponse execute(Long id) {
        return groupRepository.findById(id)
                .map(GroupResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다. id=" + id));
    }
}
