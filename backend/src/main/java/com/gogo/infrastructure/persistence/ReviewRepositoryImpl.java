package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.Review;
import com.gogo.domain.repository.ReviewRepository;
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
        return reviewJpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return reviewJpaRepository.findById(id);
    }

    @Override
    public List<Review> findByPlaceId(Long placeId) {
        return reviewJpaRepository.findByPlaceId(placeId);
    }

    @Override
    public void deleteById(Long id) {
        reviewJpaRepository.deleteById(id);
    }
}
