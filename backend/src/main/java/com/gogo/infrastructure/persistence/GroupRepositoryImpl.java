package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.Group;
import com.gogo.domain.entity.GroupMember;
import com.gogo.domain.repository.GroupRepository;
import com.gogo.infrastructure.persistence.entity.GroupJpaEntity;
import com.gogo.infrastructure.persistence.entity.GroupMemberJpaEntity;
import com.gogo.infrastructure.persistence.mapper.GroupMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GroupRepositoryImpl implements GroupRepository {

    private final GroupJpaRepository groupJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;
    private final GroupMapper groupMapper;

    public GroupRepositoryImpl(GroupJpaRepository groupJpaRepository,
                               GroupMemberJpaRepository groupMemberJpaRepository,
                               GroupMapper groupMapper) {
        this.groupJpaRepository = groupJpaRepository;
        this.groupMemberJpaRepository = groupMemberJpaRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    public Group save(Group group) {
        GroupJpaEntity savedGroup = groupJpaRepository.save(groupMapper.toJpaEntity(group));
        // 새로 추가된 멤버(id == null)만 저장하고 결과 직접 조립 (JPA 1차 캐시 우회)
        List<GroupMember> finalMembers = new ArrayList<>();
        for (GroupMember member : group.getMembers()) {
            if (member.getId() == null) {
                GroupMemberJpaEntity savedMember = groupMemberJpaRepository.save(
                        groupMapper.toMemberJpaEntity(savedGroup, member));
                finalMembers.add(new GroupMember(savedMember.getId(), savedGroup.getId(),
                        savedMember.getNickname(), savedMember.getJoinedAt()));
            } else {
                finalMembers.add(member);
            }
        }
        return Group.reconstruct(savedGroup.getId(), savedGroup.getName(), savedGroup.getInviteCode(),
                savedGroup.getCreatedBy(), finalMembers, savedGroup.getCreatedAt());
    }

    @Override
    public Optional<Group> findById(Long id) {
        return groupJpaRepository.findById(id).map(groupMapper::toDomain);
    }

    @Override
    public Optional<Group> findByInviteCode(String inviteCode) {
        return groupJpaRepository.findByInviteCode(inviteCode).map(groupMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        groupJpaRepository.deleteById(id);
    }
}
