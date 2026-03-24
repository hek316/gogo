package com.gogo.domain.dto;

import java.util.List;

public record VoteResult(Long placeId, int voteCount, List<String> voters) {}
