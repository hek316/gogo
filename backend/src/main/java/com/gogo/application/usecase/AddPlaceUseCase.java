package com.gogo.application.usecase;

import com.gogo.application.dto.AddPlaceRequest;
import com.gogo.application.dto.PlaceResponse;
import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddPlaceUseCase {

    private final PlaceRepository placeRepository;

    public AddPlaceUseCase(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public PlaceResponse execute(AddPlaceRequest request) {
        Place place = Place.create(
                request.name(),
                request.address(),
                request.category(),
                request.url(),
                request.note(),
                request.createdBy()
        );
        Place saved = placeRepository.save(place);
        return PlaceResponse.from(saved);
    }
}
