package com.gogo.application.dto;

import com.gogo.domain.entity.Meeting;
import com.gogo.domain.entity.MeetingStatus;
import com.gogo.domain.entity.MeetingVote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MeetingResponse(
        Long id,
        Long groupId,
        String title,
        List<Long> candidatePlaceIds,
        MeetingStatus status,
        Long confirmedPlaceId,
        List<VoteResult> voteResults,
        LocalDateTime createdAt
) {
    public static MeetingResponse of(Meeting meeting, List<MeetingVote> votes) {
        Map<Long, List<MeetingVote>> byPlace = votes.stream()
                .collect(Collectors.groupingBy(MeetingVote::getPlaceId));

        List<VoteResult> results = meeting.getCandidatePlaceIds().stream()
                .map(placeId -> {
                    List<MeetingVote> placeVotes = byPlace.getOrDefault(placeId, List.of());
                    return new VoteResult(placeId, placeVotes.size(),
                            placeVotes.stream().map(MeetingVote::getVoterName).toList());
                })
                .toList();

        return new MeetingResponse(
                meeting.getId(), meeting.getGroupId(), meeting.getTitle(),
                meeting.getCandidatePlaceIds(), meeting.getStatus(),
                meeting.getConfirmedPlaceId(), results, meeting.getCreatedAt()
        );
    }
}
