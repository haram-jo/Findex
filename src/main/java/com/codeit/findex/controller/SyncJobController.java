package com.codeit.findex.controller;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

    private final SyncJobService syncJobService;

    // 지수 정보 연동
    @PostMapping("/index-infos")
    public ResponseEntity<List<SyncJobDto>> createIndexInfoSyncJob(HttpServletRequest request) {
        String workerId = request.getRemoteAddr();
        List<SyncJobDto> response = syncJobService.createIndexInfoSyncJob(workerId);
        return ResponseEntity.ok(response);
    }

    // 지수 데이터 연동
    @PostMapping("/index-data")
    public ResponseEntity<List<SyncJobDto>> createIndexDataSyncJob(
            HttpServletRequest request,
            @Valid @RequestBody IndexDataSyncRequest syncData) {
        String workerId = request.getRemoteAddr();
        List<SyncJobDto> response = syncJobService.createIndexDataSyncJob(workerId, syncData);
        return ResponseEntity.ok(response);
    }

    // 연동 작업 목록 조회
    @GetMapping
    public ResponseEntity<MarketIndexApiResponse> list(SyncJobSearchRequest param) {
        return ResponseEntity.ok(syncJobService.findAll(param));
    }
}
