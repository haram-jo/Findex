package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.ChartPeriodType;
import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.data.MajorIndexDto;

import java.util.List;

public interface DashBoardRepositoryCustom {
    List<MajorIndexDto> getFavoriteMajorIndexData(int month);

    IndexChartDto findIndexChartData(Long indexInfoId, ChartPeriodType periodType);

    List<MajorIndexDto> getCurrentAndPreviousMonthData(int month);
}
