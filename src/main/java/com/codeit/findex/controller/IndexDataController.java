package com.codeit.findex.controller;

import com.codeit.findex.dto.data.CursorPageResponseIndexDataDto;
import com.codeit.findex.dto.data.IndexDataDto;
import com.codeit.findex.dto.request.IndexDataCreateRequest;
import com.codeit.findex.dto.request.IndexDataSearchCondition;
import com.codeit.findex.dto.request.IndexDataUpdateRequest;
import com.codeit.findex.service.IndexDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-data")
public class IndexDataController {

    private final IndexDataService indexDataService;

    @GetMapping
    public ResponseEntity<CursorPageResponseIndexDataDto> searchIndexData(
            @ModelAttribute IndexDataSearchCondition condition) {
        CursorPageResponseIndexDataDto response = indexDataService.searchIndexData(condition);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IndexDataDto> createIndexData(
            @Valid @RequestBody IndexDataCreateRequest request) {

        IndexDataDto response = indexDataService.createIndexData(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexData(@PathVariable Long id) {
        indexDataService.deleteIndexData(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IndexDataDto> updateIndexData(
            @PathVariable Long id,
            @Valid @RequestBody IndexDataUpdateRequest request) {
        IndexDataDto response = indexDataService.updateIndexData(id, request);
        return ResponseEntity.ok(response);
    }
}