package com.gogo.application.usecase;

import com.gogo.application.dto.GroupPlaceResponse;
import com.gogo.application.dto.SharePlaceRequest;
import com.gogo.domain.entity.GroupPlace;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.GroupPlaceRepository;
import com.gogo.domain.repository.PlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SharePlaceToGroupUseCaseTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private GroupPlaceRepository groupPlaceRepository;

    @InjectMocks
    private SharePlaceToGroupUseCase sharePlaceToGroupUseCase;

    @Test
    void 장소_공유_성공() {
        Place place = Place.create("성수동 카페", "서울", "CAFE", null, null, null, "홍길동");
        given(placeRepository.findById(1L)).willReturn(Optional.of(place));
        GroupPlace saved = GroupPlace.create(1L, 1L, "홍길동");
        given(groupPlaceRepository.save(any())).willReturn(saved);

        GroupPlaceResponse response = sharePlaceToGroupUseCase.execute(new SharePlaceRequest(1L, 1L, "홍길동"));

        assertThat(response).isNotNull();
    }

    @Test
    void 존재하지_않는_장소_공유시_예외() {
        given(placeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sharePlaceToGroupUseCase.execute(new SharePlaceRequest(1L, 999L, "홍길동")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("장소");
    }
}
