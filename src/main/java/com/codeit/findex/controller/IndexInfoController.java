package com.codeit.findex.controller;


import com.codeit.findex.dto.data.CursorPageResponseIndexInfoDto;
import com.codeit.findex.dto.data.IndexInfoDto;
import com.codeit.findex.dto.data.IndexInfoSummaryDto;
import com.codeit.findex.dto.request.IndexInfoCreateRequest;
import com.codeit.findex.dto.request.IndexInfoUpdateRequest;
import com.codeit.findex.service.IndexInfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  public CursorPageResponseIndexInfoDto getIndexInfoList(
      @RequestParam(required = false) String indexClassification,
      @RequestParam(required = false) String indexName,
      @RequestParam(required = false) Boolean favorite,
      @RequestParam(required = false) Long idAfter, // 예: id =10까지 봤으니, 그 다음 보려면 idAfter=10
      @RequestParam(required = false) String cursor, // 다음 페이지 조회를 위해 사용하는 값
      @RequestParam(required = false, defaultValue = "indexClassification") String sortField,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection, // 오름차순
      @RequestParam(required = false, defaultValue = "10") Integer size
  ) {
    return indexInfoService.getIndexInfoList(
        indexClassification, indexName, favorite,
        idAfter, cursor, sortField, sortDirection, size
    );
  }

  //지수 요약 목록 조회
  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexInfoSummaries() {
    return ResponseEntity.ok(indexInfoService.getIndexInfoSummaries());
  }
}
