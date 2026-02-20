package com.gogo.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddReviewRequest(
        @NotBlank String authorName,
        @Min(1) @Max(5) int rating,
        String content,
        @NotNull LocalDate visitedAt
) {}
