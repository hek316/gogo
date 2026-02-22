package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.PlaceStatus;
import com.gogo.infrastructure.persistence.entity.PlaceJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceJpaRepository extends JpaRepository<PlaceJpaEntity, Long> {
    List<PlaceJpaEntity> findByCategory(String category);
    List<PlaceJpaEntity> findByStatus(PlaceStatus status);

    @Query(value = "SELECT p.* FROM places p LEFT JOIN group_places gp ON gp.place_id = p.id " +
            "GROUP BY p.id ORDER BY COUNT(gp.id) DESC, p.created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<PlaceJpaEntity> findPopularPlaces(@Param("limit") int limit);

    List<PlaceJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
