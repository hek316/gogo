package com.gogo.application.service;

import com.gogo.application.dto.GroupResponse;
import com.gogo.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GroupQueryService {

    private final GroupRepository groupRepository;

    public GroupQueryService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupResponse getGroup(Long id) {
        return groupRepository.findById(id)
                .map(GroupResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다. id=" + id));
    }
}
