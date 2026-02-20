package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.GroupPlaceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupPlaceJpaRepository extends JpaRepository<GroupPlaceJpaEntity, Long> {
    List<GroupPlaceJpaEntity> findByGroupId(Long groupId);
    boolean existsByGroupIdAndPlaceId(Long groupId, Long placeId);
}
