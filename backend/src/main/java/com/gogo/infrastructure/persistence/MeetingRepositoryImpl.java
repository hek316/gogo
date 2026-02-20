package com.gogo.infrastructure.persistence;

import com.gogo.domain.entity.Meeting;
import com.gogo.domain.repository.MeetingRepository;
import com.gogo.infrastructure.persistence.entity.MeetingCandidateJpaEntity;
import com.gogo.infrastructure.persistence.entity.MeetingJpaEntity;
import com.gogo.infrastructure.persistence.mapper.MeetingMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MeetingRepositoryImpl implements MeetingRepository {

    private final MeetingJpaRepository meetingJpaRepository;
    private final MeetingCandidateJpaRepository candidateJpaRepository;
    private final MeetingMapper meetingMapper;

    public MeetingRepositoryImpl(MeetingJpaRepository meetingJpaRepository,
                                  MeetingCandidateJpaRepository candidateJpaRepository,
                                  MeetingMapper meetingMapper) {
        this.meetingJpaRepository = meetingJpaRepository;
        this.candidateJpaRepository = candidateJpaRepository;
        this.meetingMapper = meetingMapper;
    }

    @Override
    public Meeting save(Meeting meeting) {
        MeetingJpaEntity saved = meetingJpaRepository.save(meetingMapper.toJpaEntity(meeting));

        // 후보 장소 저장 (신규 meeting만 - candidates가 DB에 없는 경우)
        if (meeting.getId() == null) {
            for (Long placeId : meeting.getCandidatePlaceIds()) {
                candidateJpaRepository.save(new MeetingCandidateJpaEntity(saved, placeId));
            }
        }

        // 직접 조립해서 반환 (JPA 1차 캐시 우회)
        return Meeting.reconstruct(saved.getId(), saved.getGroupId(), saved.getTitle(),
                new ArrayList<>(meeting.getCandidatePlaceIds()),
                saved.getStatus(), saved.getConfirmedPlaceId(), saved.getCreatedAt());
    }

    @Override
    public Optional<Meeting> findById(Long id) {
        return meetingJpaRepository.findById(id).map(meetingMapper::toDomain);
    }

    @Override
    public List<Meeting> findByGroupId(Long groupId) {
        return meetingJpaRepository.findByGroupId(groupId).stream()
                .map(meetingMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        meetingJpaRepository.deleteById(id);
    }
}
