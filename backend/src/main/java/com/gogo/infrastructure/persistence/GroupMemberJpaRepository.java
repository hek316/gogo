package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.GroupMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberJpaEntity, Long> {
}
