package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;

    public PlaceRepositoryImpl(PlaceJpaRepository placeJpaRepository) {
        this.placeJpaRepository = placeJpaRepository;
    }

    @Override
    public Place save(Place place) {
        return placeJpaRepository.save(place);
    }

    @Override
    public Optional<Place> findById(Long id) {
        return placeJpaRepository.findById(id);
    }

    @Override
    public List<Place> findAll() {
        return placeJpaRepository.findAll();
    }

    @Override
    public List<Place> findByCategory(String category) {
        return placeJpaRepository.findByCategory(category);
    }

    @Override
    public void deleteById(Long id) {
        placeJpaRepository.deleteById(id);
    }

    @Override
    public List<Place> findPopularPlaces(int limit) {
        return placeJpaRepository.findPopularPlaces(limit);
    }

    @Override
    public List<Place> findRecent(int limit) {
        return placeJpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
}
