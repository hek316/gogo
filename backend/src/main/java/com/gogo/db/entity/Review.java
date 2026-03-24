package com.gogo.db.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate visitedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Review() {}

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
