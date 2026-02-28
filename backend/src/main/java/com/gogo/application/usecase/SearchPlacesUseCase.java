package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceSearchResult;
import com.gogo.infrastructure.external.NaverLocalApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchPlacesUseCase {

    private final NaverLocalApiClient naverLocalApiClient;

    public SearchPlacesUseCase(NaverLocalApiClient naverLocalApiClient) {
        this.naverLocalApiClient = naverLocalApiClient;
    }

    public List<PlaceSearchResult> execute(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return naverLocalApiClient.search(keyword.trim());
    }
}
