package com.gogo.application.service;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.port.AuthContext;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceLikeRepository;
import com.gogo.domain.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlaceQueryServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private PlaceLikeRepository placeLikeRepository;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private PlaceQueryService placeQueryService;

    @BeforeEach
    void setUp() {
        given(authContext.currentUserId()).willReturn(Optional.of(1L));
    }

    @Test
    void getAllPlaces() {
        given(placeRepository.findAll()).willReturn(List.of(
                Place.create("CafeA", "Seoul", "CAFE", null, null, null, "ownerA"),
                Place.create("FoodB", "Seoul", "RESTAURANT", null, null, null, "ownerB")
        ));

        List<PlaceResponse> result = placeQueryService.getPlaces(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void getPlacesByCategory() {
        given(placeRepository.findByCategory("CAFE")).willReturn(List.of(
                Place.create("CafeA", "Seoul", "CAFE", null, null, null, "ownerA")
        ));

        List<PlaceResponse> result = placeQueryService.getPlaces("CAFE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("CAFE");
    }
}
