package com.gogo.presentation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.application.usecase.AddPlaceUseCase;
import com.gogo.application.usecase.DeletePlaceUseCase;
import com.gogo.application.usecase.FetchPlacePreviewUseCase;
import com.gogo.application.usecase.GetPlaceUseCase;
import com.gogo.application.usecase.GetPlacesUseCase;
import com.gogo.application.usecase.GetPopularPlacesUseCase;
import com.gogo.application.usecase.GetRecentPlacesUseCase;
import com.gogo.application.usecase.MarkPlaceVisitedUseCase;
import com.gogo.domain.entity.PlaceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlacesController.class)
class PlacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddPlaceUseCase addPlaceUseCase;

    @MockitoBean
    private GetPlacesUseCase getPlacesUseCase;

    @MockitoBean
    private GetPlaceUseCase getPlaceUseCase;

    @MockitoBean
    private DeletePlaceUseCase deletePlaceUseCase;

    @MockitoBean
    private MarkPlaceVisitedUseCase markPlaceVisitedUseCase;

    @MockitoBean
    private GetPopularPlacesUseCase getPopularPlacesUseCase;

    @MockitoBean
    private GetRecentPlacesUseCase getRecentPlacesUseCase;

    @MockitoBean
    private FetchPlacePreviewUseCase fetchPlacePreviewUseCase;

    @Test
    void POST_api_places_성공() throws Exception {
        PlaceResponse response = new PlaceResponse(1L, "성수동 카페", "서울 성동구", "CAFE", null, null, null, PlaceStatus.WANT_TO_GO, "홍길동", LocalDateTime.now());
        given(addPlaceUseCase.execute(any())).willReturn(response);

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "성수동 카페", "address", "서울 성동구", "category", "CAFE", "createdBy", "홍길동")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("성수동 카페"));
    }

    @Test
    void GET_api_places_목록_반환() throws Exception {
        PlaceResponse place = new PlaceResponse(1L, "카페A", "서울", "CAFE", null, null, null, PlaceStatus.WANT_TO_GO, "홍길동", LocalDateTime.now());
        given(getPlacesUseCase.execute(isNull())).willReturn(List.of(place));

        mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("카페A"));
    }
}
