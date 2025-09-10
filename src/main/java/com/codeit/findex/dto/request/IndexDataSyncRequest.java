package com.codeit.findex.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record IndexDataSyncRequest(

        @NotNull(message = "지수 ID는 필수입니다.")
        List<Long> indexInfoIds,

        String baseDateFrom,

        String baseDateTo
) {}
