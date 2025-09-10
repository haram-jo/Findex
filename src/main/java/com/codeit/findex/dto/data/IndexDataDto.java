package com.codeit.findex.dto.data;

import com.codeit.findex.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record IndexDataDto(
    Long id,
    Long indexInfoId,
    LocalDate baseDate,
    SourceType sourceType,
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount
) {}