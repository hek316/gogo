package com.gogo.application.service;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.Place;
import com.gogo.domain.entity.PlaceStatus;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.domain.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlaceCommandServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private PlaceLikeRepository placeLikeRepository;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private PlaceCommandService placeCommandService;

    @Test
    void addPlaceSuccess() {
        given(authContext.currentNickname()).willReturn(Optional.of("tester"));
        AddPlaceRequest request = new AddPlaceRequest("Cafe", "Seoul", "CAFE", "https://naver.me/xxx", "good", null);
        Place saved = Place.create(request.name(), request.address(), request.category(), request.url(), request.note(), request.imageUrl(), "tester");
        given(placeRepository.save(any(Place.class))).willReturn(saved);

        PlaceResponse response = placeCommandService.addPlace(request);

        assertThat(response.name()).isEqualTo("Cafe");
        assertThat(response.status()).isEqualTo(PlaceStatus.WANT_TO_GO);
    }

    @Test
    void addPlaceWithoutNameThrows() {
        AddPlaceRequest request = new AddPlaceRequest("", "Seoul", "CAFE", null, null, null);

        assertThatThrownBy(() -> placeCommandService.addPlace(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markVisitedSuccess() {
        given(authContext.currentUserId()).willReturn(Optional.of(1L));
        Place place = Place.create("Cafe", "Seoul", "CAFE", null, null, null, "tester");
        given(placeRepository.findById(1L)).willReturn(Optional.of(place));
        given(placeRepository.save(any(Place.class))).willReturn(place);

        PlaceResponse response = placeCommandService.markVisited(1L);

        assertThat(response.status()).isEqualTo(PlaceStatus.VISITED);
    }

    @Test
    void markVisitedNotFoundThrows() {
        given(placeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> placeCommandService.markVisited(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
