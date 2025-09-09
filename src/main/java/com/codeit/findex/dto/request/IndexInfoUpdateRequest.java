package com.codeit.findex.dto.request;
import lombok.*;

/* 지수 수정하는
 * 요청 DTO
 */

public record IndexInfoUpdateRequest(
    Integer employedItemsCount,   // 기준이 되는 날짜
    String basePointInTime, // 채용 종목수
    Double baseIndex, //지수 산출 값
    Boolean favorite
) {}