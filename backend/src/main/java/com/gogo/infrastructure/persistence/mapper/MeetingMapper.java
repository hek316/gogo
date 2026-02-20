package com.gogo.infrastructure.persistence.mapper;

import com.gogo.domain.entity.Meeting;
import com.gogo.infrastructure.persistence.entity.MeetingJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeetingMapper {

    public Meeting toDomain(MeetingJpaEntity entity) {
        List<Long> candidatePlaceIds = entity.getCandidates().stream()
                .map(c -> c.getPlaceId())
                .toList();
        return Meeting.reconstruct(
                entity.getId(), entity.getGroupId(), entity.getTitle(),
                candidatePlaceIds, entity.getStatus(),
                entity.getConfirmedPlaceId(), entity.getCreatedAt()
        );
    }

    public MeetingJpaEntity toJpaEntity(Meeting meeting) {
        return new MeetingJpaEntity(
                meeting.getId(), meeting.getGroupId(), meeting.getTitle(),
                meeting.getStatus(), meeting.getConfirmedPlaceId(), meeting.getCreatedAt()
        );
    }
}
