package com.codeit.findex.dto.data;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record MajorIndexDto(
        Long indexInfoId,
        String indexClassification,
        String indexName,
        LocalDate baseDate,
        BigDecimal closingPrice
) {}
