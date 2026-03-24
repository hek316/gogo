package com.gogo.db.repository;

import com.gogo.db.entity.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByCategory(String category);

    @Query(value = "SELECT p.* FROM places p " +
            "LEFT JOIN group_places gp ON gp.place_id = p.id " +
            "LEFT JOIN place_likes pl ON pl.place_id = p.id " +
            "GROUP BY p.id " +
            "ORDER BY (COUNT(DISTINCT gp.id) + COUNT(DISTINCT pl.id) * 0.5) DESC, p.created_at DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Place> findPopularPlaces(@Param("limit") int limit);

    List<Place> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
