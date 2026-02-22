package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetRecentPlacesUseCase {

    private final PlaceRepository placeRepository;

    public GetRecentPlacesUseCase(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<PlaceResponse> execute(int limit) {
        return placeRepository.findRecent(limit).stream()
                .map(PlaceResponse::from)
                .toList();
    }
}
