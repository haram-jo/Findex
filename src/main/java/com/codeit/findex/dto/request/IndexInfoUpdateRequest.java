package com.codeit.findex.dto.request;
import lombok.*;

/* 지수 수정하는
 * 요청 DTO
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoUpdateRequest {

  private Integer employedItemsCount;   // 기준이 되는 날짜
  private String basePointInTime; // 채용 종목수
  private Double baseIndex; //지수 산출 값
  private Boolean favorite;
}