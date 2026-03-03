package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    List<Review> findByPlaceId(Long placeId);
}
