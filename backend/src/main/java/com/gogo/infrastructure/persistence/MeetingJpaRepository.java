package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.MeetingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingJpaRepository extends JpaRepository<MeetingJpaEntity, Long> {
    List<MeetingJpaEntity> findByGroupId(Long groupId);
}
