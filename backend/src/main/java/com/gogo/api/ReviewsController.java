package com.gogo.api;

import com.gogo.domain.dto.AddReviewRequest;
import com.gogo.domain.dto.ReviewResponse;
import com.gogo.domain.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places/{placeId}/reviews")
public class ReviewsController {

    private final ReviewService reviewService;

    public ReviewsController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long placeId,
                                                     @Valid @RequestBody AddReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.addReview(placeId, request));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long placeId) {
        return ResponseEntity.ok(reviewService.getReviews(placeId));
    }
}
