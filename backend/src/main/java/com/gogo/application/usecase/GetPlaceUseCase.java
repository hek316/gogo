package com.gogo.application.usecase;

import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetPlaceUseCase {

    private final PlaceRepository placeRepository;

    public GetPlaceUseCase(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public PlaceResponse execute(Long id) {
        return placeRepository.findById(id)
                .map(PlaceResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
    }
}
