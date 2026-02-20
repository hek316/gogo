package com.gogo.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Review {

    private Long id;
    private Long placeId;
    private String authorName;
    private int rating;
    private String content;
    private LocalDate visitedAt;
    private LocalDateTime createdAt;

    private Review() {}

    public static Review create(Long placeId, String authorName, int rating, String content, LocalDate visitedAt) {
        validate(rating);
        Review review = new Review();
        review.placeId = placeId;
        review.authorName = authorName;
        review.rating = rating;
        review.content = content;
        review.visitedAt = visitedAt;
        review.createdAt = LocalDateTime.now();
        return review;
    }

    public static Review reconstruct(Long id, Long placeId, String authorName, int rating,
                                     String content, LocalDate visitedAt, LocalDateTime createdAt) {
        Review review = new Review();
        review.id = id;
        review.placeId = placeId;
        review.authorName = authorName;
        review.rating = rating;
        review.content = content;
        review.visitedAt = visitedAt;
        review.createdAt = createdAt;
        return review;
    }

    private static void validate(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
        }
    }

    public Long getId() { return id; }
    public Long getPlaceId() { return placeId; }
    public String getAuthorName() { return authorName; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public LocalDate getVisitedAt() { return visitedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
