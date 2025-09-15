package com.codeit.findex.controller;


import com.codeit.findex.dto.data.CursorPageResponseIndexInfoDto;
import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.data.IndexInfoSummaryDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.dto.request.IndexInfoSearchRequest;
import com.codeit.findex.dto.request.IndexInfoUpdateRequest;
import com.codeit.findex.service.IndexInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* 지수 정보 컨트롤러
*/

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  //지수 등록
  @PostMapping
  public ResponseEntity<IndexInfoDto> createIndexInfo(@RequestBody IndexInfoCreateRequest request) {
    IndexInfoDto response = indexInfoService.createIndexInfo(request);
    return ResponseEntity.status(201).body(response);
  }

  //지수 수정
  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoDto> updateIndexInfo(@PathVariable Long id, @RequestBody IndexInfoUpdateRequest request) {
    IndexInfoDto response = indexInfoService.updateIndexInfo(id, request);
    return ResponseEntity.ok(response);
  }

  //지수 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteIndexInfo(@PathVariable Long id) {
    indexInfoService.deleteIndexInfo(id);
    return ResponseEntity.noContent().build(); //204 No Content 반환
  }

  //지수 조회 (단건)
  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> getIndexInfo(@PathVariable Long id) {
    IndexInfoDto response = indexInfoService.getIndexInfo(id);
    return ResponseEntity.ok(response);
  }

  //지수 목록 조회 (다건)
  @GetMapping
  public ResponseEntity<CursorPageResponseIndexInfoDto> getIndexInfoList(IndexInfoSearchRequest param) {
        CursorPageResponseIndexInfoDto response = indexInfoService.getIndexInfoList(param);
    return ResponseEntity.ok(response);
  }

  //지수 요약 목록 조회
  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexInfoSummaries() {
    return ResponseEntity.ok(indexInfoService.getIndexInfoSummaries());
  }
}
