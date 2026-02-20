package com.gogo.presentation.api;

import com.gogo.application.dto.AddReviewRequest;
import com.gogo.application.dto.ReviewResponse;
import com.gogo.application.usecase.AddReviewUseCase;
import com.gogo.application.usecase.GetReviewsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewsController {

    private final AddReviewUseCase addReviewUseCase;
    private final GetReviewsUseCase getReviewsUseCase;

    public ReviewsController(AddReviewUseCase addReviewUseCase, GetReviewsUseCase getReviewsUseCase) {
        this.addReviewUseCase = addReviewUseCase;
        this.getReviewsUseCase = getReviewsUseCase;
    }

    // 후기 작성
    @PostMapping("/api/places/{placeId}/reviews")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long placeId,
                                                     @Valid @RequestBody AddReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addReviewUseCase.execute(placeId, request));
    }

    // 후기 목록
    @GetMapping("/api/places/{placeId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long placeId) {
        return ResponseEntity.ok(getReviewsUseCase.execute(placeId));
    }
}
