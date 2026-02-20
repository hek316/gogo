package com.gogo.application.usecase;

import com.gogo.application.dto.AddReviewRequest;
import com.gogo.application.dto.ReviewResponse;
import com.gogo.domain.entity.Review;
import com.gogo.domain.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddReviewUseCase {

    private final ReviewRepository reviewRepository;

    public AddReviewUseCase(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public ReviewResponse execute(Long placeId, AddReviewRequest request) {
        Review review = Review.create(placeId, request.authorName(), request.rating(),
                request.content(), request.visitedAt());
        Review saved = reviewRepository.save(review);
        return ReviewResponse.from(saved);
    }
}
