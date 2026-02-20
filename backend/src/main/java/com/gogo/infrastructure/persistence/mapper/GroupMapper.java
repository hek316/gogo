package com.gogo.infrastructure.persistence.mapper;

import com.gogo.domain.entity.Group;
import com.gogo.domain.entity.GroupMember;
import com.gogo.infrastructure.persistence.entity.GroupJpaEntity;
import com.gogo.infrastructure.persistence.entity.GroupMemberJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupMapper {

    public Group toDomain(GroupJpaEntity entity) {
        List<GroupMember> members = entity.getMembers().stream()
                .map(m -> new GroupMember(m.getId(), entity.getId(), m.getNickname(), m.getJoinedAt()))
                .toList();
        return Group.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getInviteCode(),
                entity.getCreatedBy(),
                members,
                entity.getCreatedAt()
        );
    }

    public GroupJpaEntity toJpaEntity(Group group) {
        return new GroupJpaEntity(
                group.getId(),
                group.getName(),
                group.getInviteCode(),
                group.getCreatedBy(),
                group.getCreatedAt()
        );
    }

    public GroupMemberJpaEntity toMemberJpaEntity(GroupJpaEntity groupEntity, GroupMember member) {
        return new GroupMemberJpaEntity(groupEntity, member.getNickname(), member.getJoinedAt());
    }
}
