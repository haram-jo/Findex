package com.codeit.findex.service;

import com.codeit.findex.dto.data.ChartPeriodType;
import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.response.MajorIndexDataResponse;

import java.util.List;

public interface DashBoardService {
    List<MajorIndexDataResponse> getMajorIndex(String periodType);

    IndexChartDto getIndexChart(Long indexInfoId, ChartPeriodType periodType);
}
