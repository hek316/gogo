package com.gogo.application.service;

import com.gogo.application.dto.AddReviewRequest;
import com.gogo.application.dto.ReviewResponse;
import com.gogo.domain.entity.Review;
import com.gogo.domain.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public ReviewResponse addReview(Long placeId, AddReviewRequest request) {
        Review review = Review.create(placeId, request.authorName(), request.rating(),
                request.content(), request.visitedAt());
        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long placeId) {
        return reviewRepository.findByPlaceId(placeId).stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
