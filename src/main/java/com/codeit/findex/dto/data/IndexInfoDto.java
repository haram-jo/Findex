package com.codeit.findex.dto.data;
import lombok.*;
import com.codeit.findex.entity.SourceType;

/*지수 단건/리스트
 *응답 DTO
 */

public record IndexInfoDto(
    Long id, // 고유 식별자

    String indexClassification, // 지수 분류명 (예: KOSPI 시리즈, KOSDAO 시리즈)
    String indexName, // 지수명
    Integer employedItemsCount, //채용 종목수
    String basePointInTime, // 지수의 기준이 되는 날짜
    Double baseIndex, // 지수 산출 기준 값
    SourceType sourceType, // 정보 출처(Open API인지, 직접 등록했는지)
    Boolean favorite // 즐겨찾기 여부
) {}

