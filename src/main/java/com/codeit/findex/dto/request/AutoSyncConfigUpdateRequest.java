package com.codeit.findex.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AutoSyncConfigUpdateRequest {
    @NotNull
    private Boolean enabled; // 활성화 여부
}
