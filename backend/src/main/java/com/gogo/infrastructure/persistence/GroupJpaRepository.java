package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.GroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, Long> {
    Optional<GroupJpaEntity> findByInviteCode(String inviteCode);
}
