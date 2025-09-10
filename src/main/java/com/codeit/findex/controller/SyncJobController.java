package com.codeit.findex.controller;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
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

    @PostMapping("/index-infos")
    public ResponseEntity<List<SyncJobDto>> createSyncJob(HttpServletRequest request) {
        String workerId = request.getRemoteAddr();
        List<SyncJobDto> response = syncJobService.createSyncJob(workerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/index-data")
    public ResponseEntity<List<SyncJobDto>> createSyncIndexData(
            HttpServletRequest request,
            @Valid @RequestBody IndexDataSyncRequest syncData) {
        String workerId = request.getRemoteAddr();
        List<SyncJobDto> response = syncJobService.createIndexDataSyncJob(workerId, syncData);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<MarketIndexApiResponse> getSyncJobs() {
        return ResponseEntity.ok(syncJobService.findAll());
    }
}
