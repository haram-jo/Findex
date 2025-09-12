package com.codeit.findex.dto.response;

import lombok.Builder;

@Builder
public record IndexDataRank(
        MajorIndexDataResponse performance,
        int rank
) {}
