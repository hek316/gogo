package com.gogo.domain.usecase;

import com.gogo.domain.dto.PlaceSearchResult;
import com.gogo.client.naver.NaverLocalApiClient;
import com.gogo.client.naver.NaverLocalApiClient.NaverSearchResult;
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
        return naverLocalApiClient.search(keyword.trim()).stream()
                .map(this::toPlaceSearchResult)
                .toList();
    }

    private PlaceSearchResult toPlaceSearchResult(NaverSearchResult item) {
        return new PlaceSearchResult(
                item.name(), item.address(), item.mapUrl(),
                item.category(), item.telephone());
    }
}
