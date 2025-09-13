package com.codeit.findex.dto.data;

import java.util.List;

public record IndexChartDto(
        Long indexInfoId,
        String indexClassification,
        String indexName,
        ChartPeriodType periodType,
        List<ChartDataPoint> dataPoints,
        List<ChartDataPoint> ma5DataPoints,
        List<ChartDataPoint> ma20DataPoints
) {}
