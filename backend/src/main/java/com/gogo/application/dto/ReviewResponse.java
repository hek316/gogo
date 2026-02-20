package com.gogo.application.dto;

import com.gogo.domain.entity.Review;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long placeId,
        String authorName,
        int rating,
        String content,
        LocalDate visitedAt,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getPlaceId(),
                review.getAuthorName(),
                review.getRating(),
                review.getContent(),
                review.getVisitedAt(),
                review.getCreatedAt()
        );
    }
}
