package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.ChartPeriodType;
import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.data.MajorIndexDto;

import java.util.List;

public interface DashBoardRepositoryCustom {
    List<MajorIndexDto> getFavoriteMajorIndexData(int month);

    IndexChartDto findIndexChartData(Long indexInfoId, ChartPeriodType periodType);

    List<MajorIndexDto> getCurrentAndPreviousMonthData(int month);

    // 최신 데이터
    MajorIndexDto getLatestMajorIndexData(Long indexInfoId);

    // 하루 전 데이터
    MajorIndexDto getBeforeDayMajorIndexData(Long indexInfoId);

    // 일주일 전 데이터
    MajorIndexDto getBeforeWeekMajorIndexData(Long indexInfoId);

    // 한달 전 데이터
    MajorIndexDto getBeforeMonthMajorIndexData(Long indexInfoId);
}
