package com.gogo.application.usecase;

import com.gogo.application.dto.AddReviewRequest;
import com.gogo.application.dto.ReviewResponse;
import com.gogo.domain.entity.Review;
import com.gogo.domain.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AddReviewUseCaseTest {

    @Mock ReviewRepository reviewRepository;
    @InjectMocks AddReviewUseCase addReviewUseCase;

    @Test
    void 후기_작성_성공() {
        Review saved = Review.reconstruct(1L, 1L, "홍길동", 5, "너무 좋아요!", LocalDate.now(), LocalDateTime.now());
        given(reviewRepository.save(any(Review.class))).willReturn(saved);

        AddReviewRequest request = new AddReviewRequest("홍길동", 5, "너무 좋아요!", LocalDate.now());
        ReviewResponse result = addReviewUseCase.execute(1L, request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.authorName()).isEqualTo("홍길동");
        assertThat(result.rating()).isEqualTo(5);
    }

    @Test
    void 별점_범위_초과_예외() {
        AddReviewRequest request = new AddReviewRequest("홍길동", 6, "좋아요", LocalDate.now());

        assertThatThrownBy(() -> addReviewUseCase.execute(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("별점");
    }

    @Test
    void 별점_0이하_예외() {
        AddReviewRequest request = new AddReviewRequest("홍길동", 0, "좋아요", LocalDate.now());

        assertThatThrownBy(() -> addReviewUseCase.execute(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("별점");
    }
}
