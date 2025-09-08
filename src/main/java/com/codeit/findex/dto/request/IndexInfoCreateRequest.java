package com.codeit.findex.dto.request;
import lombok.*;

/* 지수 등록하는
 * 요청 DTO
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoCreateRequest {

  private String indexClassification;
  private String indexName;
  private Integer employedItemsCount;
  private String basePointInTime; // YYYY-MM-DD
  private Double baseIndex;
  private Boolean favorite;
}

