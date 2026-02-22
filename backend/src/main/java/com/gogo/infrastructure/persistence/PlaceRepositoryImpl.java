package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.Place;
import com.gogo.domain.repository.PlaceRepository;
import com.gogo.infrastructure.persistence.mapper.PlaceMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;
    private final PlaceMapper placeMapper;

    public PlaceRepositoryImpl(PlaceJpaRepository placeJpaRepository, PlaceMapper placeMapper) {
        this.placeJpaRepository = placeJpaRepository;
        this.placeMapper = placeMapper;
    }

    @Override
    public Place save(Place place) {
        return placeMapper.toDomain(placeJpaRepository.save(placeMapper.toJpaEntity(place)));
    }

    @Override
    public Optional<Place> findById(Long id) {
        return placeJpaRepository.findById(id).map(placeMapper::toDomain);
    }

    @Override
    public List<Place> findAll() {
        return placeJpaRepository.findAll().stream()
                .map(placeMapper::toDomain)
                .toList();
    }

    @Override
    public List<Place> findByCategory(String category) {
        return placeJpaRepository.findByCategory(category).stream()
                .map(placeMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        placeJpaRepository.deleteById(id);
    }

    @Override
    public List<Place> findPopularPlaces(int limit) {
        return placeJpaRepository.findPopularPlaces(limit).stream()
                .map(placeMapper::toDomain)
                .toList();
    }

    @Override
    public List<Place> findRecent(int limit) {
        return placeJpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(placeMapper::toDomain)
                .toList();
    }
}
