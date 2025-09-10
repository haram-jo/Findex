package com.codeit.findex.dto.request;

import jakarta.validation.constraints.NotNull;

public record AutoSyncConfigUpdateRequest(
        @NotNull Boolean enabled // 활성화 여부
) {}
