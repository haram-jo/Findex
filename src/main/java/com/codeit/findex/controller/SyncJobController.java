package com.codeit.findex.controller;

import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.service.SyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

    private final SyncJobService syncJobService;

    @GetMapping
    public ResponseEntity<MarketIndexApiResponse> getSyncJobs() {
        return ResponseEntity.ok(syncJobService.findAll());
    }
}
