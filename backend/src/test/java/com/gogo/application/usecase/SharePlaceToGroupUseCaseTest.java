package com.gogo.application.usecase;

import com.gogo.application.dto.GroupPlaceResponse;
import com.gogo.application.dto.SharePlaceRequest;
import com.gogo.domain.entity.GroupPlace;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.GroupPlaceRepository;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.application.port.AuthContext;
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
class SharePlaceToGroupUseCaseTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private GroupPlaceRepository groupPlaceRepository;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private SharePlaceToGroupUseCase sharePlaceToGroupUseCase;

    @BeforeEach
    void setUp() {
        given(authContext.currentNickname()).willReturn(Optional.of("tester"));
    }

    @Test
    void sharePlaceSuccess() {
        Place place = Place.create("Cafe", "Seoul", "CAFE", null, null, null, "tester");
        given(placeRepository.findById(1L)).willReturn(Optional.of(place));
        GroupPlace saved = GroupPlace.create(1L, 1L, "tester");
        given(groupPlaceRepository.save(any())).willReturn(saved);

        GroupPlaceResponse response = sharePlaceToGroupUseCase.execute(new SharePlaceRequest(1L, 1L));

        assertThat(response).isNotNull();
    }

    @Test
    void sharingMissingPlaceThrows() {
        given(placeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sharePlaceToGroupUseCase.execute(new SharePlaceRequest(1L, 999L)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
