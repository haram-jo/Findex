package com.codeit.findex.controller;

import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

    private final SyncJobService syncJobService;

    @PostMapping("/index-infos")
    public ResponseEntity<String> createSyncJob(HttpServletRequest request) {
        String workerId = request.getRemoteAddr();
        syncJobService.createSyncJob(workerId);
        return ResponseEntity.ok(workerId);
    }

    @GetMapping
    public ResponseEntity<MarketIndexApiResponse> getSyncJobs() {
        return ResponseEntity.ok(syncJobService.findAll());
    }
}
