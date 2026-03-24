package com.gogo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogo.domain.dto.PlaceResponse;
import com.gogo.domain.service.PlaceCommandService;
import com.gogo.domain.service.PlaceLikeService;
import com.gogo.domain.service.PlaceQueryService;
import com.gogo.domain.usecase.FetchPlacePreviewUseCase;
import com.gogo.domain.usecase.SearchPlacesUseCase;
import com.gogo.db.entity.PlaceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlacesController.class)
class PlacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceQueryService placeQueryService;

    @MockitoBean
    private PlaceCommandService placeCommandService;

    @MockitoBean
    private FetchPlacePreviewUseCase fetchPlacePreviewUseCase;

    @MockitoBean
    private SearchPlacesUseCase searchPlacesUseCase;

    @MockitoBean
    private PlaceLikeService placeLikeService;

    @Test
    @WithMockUser
    void POST_api_places_성공() throws Exception {
        PlaceResponse response = new PlaceResponse(1L, "성수동 카페", "서울 성동구", "CAFE", null, null, null, PlaceStatus.WANT_TO_GO, "홍길동", LocalDateTime.now(), 0, false);
        given(placeCommandService.addPlace(any())).willReturn(response);

        mockMvc.perform(post("/api/places")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "성수동 카페", "address", "서울 성동구", "category", "CAFE")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("성수동 카페"));
    }

    @Test
    @WithMockUser
    void GET_api_places_목록_반환() throws Exception {
        PlaceResponse place = new PlaceResponse(1L, "카페A", "서울", "CAFE", null, null, null, PlaceStatus.WANT_TO_GO, "홍길동", LocalDateTime.now(), 0, false);
        given(placeQueryService.getPlaces(isNull())).willReturn(List.of(place));

        mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("카페A"));
    }
}
