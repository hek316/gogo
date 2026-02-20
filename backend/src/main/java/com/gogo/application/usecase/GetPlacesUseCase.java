package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetPlacesUseCase {

    private final PlaceRepository placeRepository;

    public GetPlacesUseCase(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<PlaceResponse> execute(String category) {
        if (category != null && !category.isBlank()) {
            return placeRepository.findByCategory(category).stream()
                    .map(PlaceResponse::from)
                    .toList();
        }
        return placeRepository.findAll().stream()
                .map(PlaceResponse::from)
                .toList();
    }
}
