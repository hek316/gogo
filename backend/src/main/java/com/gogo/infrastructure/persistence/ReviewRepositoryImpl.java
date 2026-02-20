package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.Review;
import com.gogo.domain.repository.ReviewRepository;
import com.gogo.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    public ReviewRepositoryImpl(ReviewJpaRepository reviewJpaRepository) {
        this.reviewJpaRepository = reviewJpaRepository;
    }

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = new ReviewJpaEntity(
                review.getId(),
                review.getPlaceId(),
                review.getAuthorName(),
                review.getRating(),
                review.getContent(),
                review.getVisitedAt(),
                review.getCreatedAt()
        );
        ReviewJpaEntity saved = reviewJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return reviewJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Review> findByPlaceId(Long placeId) {
        return reviewJpaRepository.findByPlaceId(placeId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        reviewJpaRepository.deleteById(id);
    }

    private Review toDomain(ReviewJpaEntity e) {
        return Review.reconstruct(e.getId(), e.getPlaceId(), e.getAuthorName(),
                e.getRating(), e.getContent(), e.getVisitedAt(), e.getCreatedAt());
    }
}
