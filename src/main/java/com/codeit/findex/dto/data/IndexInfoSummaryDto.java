package com.codeit.findex.dto.data;

/* 지수 요약목록 조회
 * 응답 DTO
 * 지수 id, 분류명, 지수명
 */

public record IndexInfoSummaryDto(
    Long id,
    String indexClassification,
    String indexName
) {

  /* JPQL에서 new로 사용할 때는 생성자를 명시적으로 선언해줘야 함
   */
  public IndexInfoSummaryDto(Long id, String indexClassification, String indexName) {
    this.id = id;
    this.indexClassification = indexClassification;
    this.indexName = indexName;
  }
}
