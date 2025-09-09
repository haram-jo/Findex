package com.codeit.findex.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataCreateRequest(

        @NotNull(message = "지수 ID는 필수입니다.")
        Long indexInfoId,

        @NotNull(message = "기준일자는 필수입니다.")
        LocalDate baseDate,

        BigDecimal marketPrice,
        BigDecimal closingPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal versus,
        BigDecimal fluctuationRate,
        Long tradingQuantity,
        Long tradingPrice,
        Long marketTotalAmount
) {
}