package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.MeetingCandidateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingCandidateJpaRepository extends JpaRepository<MeetingCandidateJpaEntity, Long> {
}
