package com.gogo.application.usecase;

import com.gogo.application.dto.ReviewResponse;
import com.gogo.domain.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetReviewsUseCase {

    private final ReviewRepository reviewRepository;

    public GetReviewsUseCase(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewResponse> execute(Long placeId) {
        return reviewRepository.findByPlaceId(placeId).stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
