package com.gogo.infrastructure.persistence;

import com.gogo.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
    List<ReviewJpaEntity> findByPlaceId(Long placeId);
}
