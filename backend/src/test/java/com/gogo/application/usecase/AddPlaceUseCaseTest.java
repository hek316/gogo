package com.gogo.application.usecase;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.entity.PlaceStatus;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.infrastructure.security.SecurityContextHelper;
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
class AddPlaceUseCaseTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private AddPlaceUseCase addPlaceUseCase;

    @BeforeEach
    void setUp() {
        given(securityContextHelper.currentNickname()).willReturn(Optional.of("tester"));
    }

    @Test
    void addPlaceSuccess() {
        AddPlaceRequest request = new AddPlaceRequest("Cafe", "Seoul", "CAFE", "https://naver.me/xxx", "good", null);
        Place saved = Place.create(request.name(), request.address(), request.category(), request.url(), request.note(), request.imageUrl(), "tester");
        given(placeRepository.save(any(Place.class))).willReturn(saved);

        PlaceResponse response = addPlaceUseCase.execute(request);

        assertThat(response.name()).isEqualTo("Cafe");
        assertThat(response.status()).isEqualTo(PlaceStatus.WANT_TO_GO);
    }

    @Test
    void addPlaceWithoutNameThrows() {
        AddPlaceRequest request = new AddPlaceRequest("", "Seoul", "CAFE", null, null, null);

        assertThatThrownBy(() -> addPlaceUseCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
