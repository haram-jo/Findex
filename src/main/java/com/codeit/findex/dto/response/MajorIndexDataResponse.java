package com.codeit.findex.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MajorIndexDataResponse(
        Long indexInfoId,
        String indexClassification,
        String indexName,
        BigDecimal versus,
        BigDecimal fluctuationRate,
        BigDecimal currentPrice,
        BigDecimal beforePrice
) {}
