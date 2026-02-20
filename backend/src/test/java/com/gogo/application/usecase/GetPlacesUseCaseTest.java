package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetPlacesUseCaseTest {

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private GetPlacesUseCase getPlacesUseCase;

    @Test
    void 전체_장소_목록_조회() {
        given(placeRepository.findAll()).willReturn(List.of(
                Place.create("카페A", "서울", "CAFE", null, null, "홍길동"),
                Place.create("식당B", "서울", "RESTAURANT", null, null, "김철수")
        ));

        List<PlaceResponse> result = getPlacesUseCase.execute(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void 카테고리별_필터링() {
        given(placeRepository.findByCategory("CAFE")).willReturn(List.of(
                Place.create("카페A", "서울", "CAFE", null, null, "홍길동")
        ));

        List<PlaceResponse> result = getPlacesUseCase.execute("CAFE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("CAFE");
    }
}
