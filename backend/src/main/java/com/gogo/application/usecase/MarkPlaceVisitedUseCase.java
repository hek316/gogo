package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkPlaceVisitedUseCase {

    private final PlaceRepository placeRepository;

    public MarkPlaceVisitedUseCase(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public PlaceResponse execute(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        place.markAsVisited();
        Place saved = placeRepository.save(place);
        return PlaceResponse.from(saved);
    }
}
