package com.gogo.infrastructure.persistence.mapper;

import com.gogo.domain.entity.Place;
import com.gogo.infrastructure.persistence.entity.PlaceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PlaceMapper {

    public PlaceJpaEntity toJpaEntity(Place place) {
        return new PlaceJpaEntity(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getCategory(),
                place.getUrl(),
                place.getNote(),
                place.getStatus(),
                place.getCreatedBy(),
                place.getCreatedAt()
        );
    }

    public Place toDomain(PlaceJpaEntity entity) {
        return Place.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getCategory(),
                entity.getUrl(),
                entity.getNote(),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }
}
