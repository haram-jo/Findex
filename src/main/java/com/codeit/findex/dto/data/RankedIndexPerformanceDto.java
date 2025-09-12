package com.codeit.findex.dto.data;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RankedIndexPerformanceDto(
     Long indexInfoId,
     String indexClassification,
     String indexName,
     BigDecimal versus,
     BigDecimal fluctuationRate,
     BigDecimal currentPrice,
     BigDecimal beforePrice
) {}
