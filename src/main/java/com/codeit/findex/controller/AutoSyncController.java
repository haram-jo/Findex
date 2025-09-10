package com.codeit.findex.controller;

import com.codeit.findex.dto.data.CursorPageResponseAutoSyncConfigDto;
import com.codeit.findex.service.AutoSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncController {

    private final AutoSyncService autoSyncService;

    @GetMapping
    public ResponseEntity<CursorPageResponseAutoSyncConfigDto> list(
            @RequestParam(required = false) Long indexInfoId,   // 지수 필터 (null=전체)
            @RequestParam(required = false) Boolean enabled,    // 활성화 필터 (null=전체)
            @RequestParam(required = false) Long idAfter,       // 이전 페이지 마지막 ID
            @RequestParam(required = false) String cursor,      // 커서(우선)
            @RequestParam(defaultValue = "indexInfo.indexName") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        var res = autoSyncService.list(indexInfoId, enabled, idAfter, cursor, sortField, sortDirection, size);
        return ResponseEntity.ok(res);
    }
}
