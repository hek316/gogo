package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.PlaceStatus;
import com.gogo.infrastructure.persistence.entity.PlaceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceJpaRepository extends JpaRepository<PlaceJpaEntity, Long> {
    List<PlaceJpaEntity> findByCategory(String category);
    List<PlaceJpaEntity> findByStatus(PlaceStatus status);
}
