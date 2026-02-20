package com.gogo.application.usecase;

import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeletePlaceUseCase {

    private final PlaceRepository placeRepository;

    public DeletePlaceUseCase(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public void execute(Long id) {
        placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
        placeRepository.deleteById(id);
    }
}
