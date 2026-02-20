package com.gogo.domain.repository;

import com.gogo.domain.entity.Place;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {
    Place save(Place place);
    Optional<Place> findById(Long id);
    List<Place> findAll();
    List<Place> findByCategory(String category);
    void deleteById(Long id);
}
