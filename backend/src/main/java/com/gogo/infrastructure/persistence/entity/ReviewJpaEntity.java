package com.gogo.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class ReviewJpaEntity {

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

    protected ReviewJpaEntity() {}

    public ReviewJpaEntity(Long id, Long placeId, String authorName, int rating,
                           String content, LocalDate visitedAt, LocalDateTime createdAt) {
        this.id = id;
        this.placeId = placeId;
        this.authorName = authorName;
        this.rating = rating;
        this.content = content;
        this.visitedAt = visitedAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getPlaceId() { return placeId; }
    public String getAuthorName() { return authorName; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public LocalDate getVisitedAt() { return visitedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
