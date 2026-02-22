package com.gogo.application.usecase;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.entity.PlaceStatus;
import com.gogo.domain.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AddPlaceUseCaseTest {

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private AddPlaceUseCase addPlaceUseCase;

    @Test
    void 유효한_데이터로_장소_추가_성공() {
        AddPlaceRequest request = new AddPlaceRequest("성수동 카페", "서울 성동구", "CAFE", "https://naver.me/xxx", "분위기 좋음", null, "홍길동");

        Place saved = Place.create(request.name(), request.address(), request.category(), request.url(), request.note(), request.imageUrl(), request.createdBy());
        given(placeRepository.save(any(Place.class))).willReturn(saved);

        PlaceResponse response = addPlaceUseCase.execute(request);

        assertThat(response.name()).isEqualTo("성수동 카페");
        assertThat(response.status()).isEqualTo(PlaceStatus.WANT_TO_GO);
    }

    @Test
    void 이름없는_장소_추가시_예외() {
        AddPlaceRequest request = new AddPlaceRequest("", "서울 성동구", "CAFE", null, null, null, "홍길동");

        assertThatThrownBy(() -> addPlaceUseCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
