package com.codeit.findex.controller;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.service.SyncJobService;
import com.codeit.findex.service.basic.IndexDataSyncService;
import com.codeit.findex.service.basic.IndexInfoSyncService;
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

    private final IndexInfoSyncService indexInfoSyncService;
    private final IndexDataSyncService indexDataSyncService;
    private final SyncJobService syncJobService;

    // 지수 정보 연동
    @PostMapping("/index-infos")
    public ResponseEntity<List<SyncJobDto>> createIndexInfoSyncJob(HttpServletRequest request) {
        String workerId = request.getRemoteAddr();
        // 1. OpenAPI 에서 가져온 데이터로 지수 정보 생성 및 저장
        indexInfoSyncService.createIndexInfos();
        // 2. 생성한 데이터 연동 작업 테이블에 저장
        List<SyncJobDto> response = syncJobService.createSyncJobsOfIndexInfo(workerId);
        return ResponseEntity.ok(response);
    }

    // 지수 데이터 연동
    @PostMapping("/index-data")
    public ResponseEntity<List<SyncJobDto>> createIndexDataSyncJob( HttpServletRequest request, @Valid @RequestBody IndexDataSyncRequest syncData) {
        // 작업자 IP 주소
        String workerId = request.getRemoteAddr();
        // 1. OpenAPI 에서 가져온 데이터를 지수 데이터 저장
        indexDataSyncService.createIndexData(syncData);
        //2. 데이터가 생성되면 연동 작업 테이블에 기록 남기기
        List<SyncJobDto> response = syncJobService.createSyncJobsOfIndexData(workerId, syncData);
        return ResponseEntity.ok(response);
    }

    // 연동 작업 목록 조회
    @GetMapping
    public ResponseEntity<CursorPageResponseSyncJobDto> list(SyncJobSearchRequest param) {
        return ResponseEntity.ok(syncJobService.findAll(param));
    }
}
