package com.gogo.application.usecase;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.entity.PlaceStatus;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.infrastructure.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AddPlaceUseCaseTest {

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private AddPlaceUseCase addPlaceUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        AuthenticatedUser principal = new AuthenticatedUser(1L, "홍길동");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 유효한_데이터로_장소_추가_성공() {
        AddPlaceRequest request = new AddPlaceRequest("성수동 카페", "서울 성동구", "CAFE", "https://naver.me/xxx", "분위기 좋음", null);

        Place saved = Place.create(request.name(), request.address(), request.category(), request.url(), request.note(), request.imageUrl(), "홍길동");
        given(placeRepository.save(any(Place.class))).willReturn(saved);

        PlaceResponse response = addPlaceUseCase.execute(request);

        assertThat(response.name()).isEqualTo("성수동 카페");
        assertThat(response.status()).isEqualTo(PlaceStatus.WANT_TO_GO);
    }

    @Test
    void 이름없는_장소_추가시_예외() {
        AddPlaceRequest request = new AddPlaceRequest("", "서울 성동구", "CAFE", null, null, null);

        assertThatThrownBy(() -> addPlaceUseCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
