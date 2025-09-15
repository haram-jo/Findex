package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.ChartPeriodType;
import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.dto.response.IndexDataRank;
import com.codeit.findex.dto.response.MajorIndexDataResponse;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.repository.DashBoardRepository;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.DashBoardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicDashBoardService implements DashBoardService {

    private final IndexInfoRepository indexInfoRepository;
    private final DashBoardRepository dashBoardRepository;
    private final IndexDataRepository indexDataRepository;

    /**
     * 주요 지수
     * - 즐겨찾기한 지수들의 등락률 대비를 가져와서 계산
     * @param periodType DAILY, WEEKLY, MONTHLY
     */
    @Override
    public List<MajorIndexDataResponse> getMajorIndex(String periodType) {

        List<Long> favoriteIds = indexInfoRepository.findByFavoriteTrue()
                .stream()
                .map(IndexInfo::getId)
                .toList();

        List<MajorIndexDataResponse> response = null;

        if(periodType.equals("DAILY")) response = dailyMajorIndex(favoriteIds);
        if(periodType.equals("WEEKLY")) response = weeklyMajorIndex(favoriteIds);
        if(periodType.equals("MONTHLY")) response = monthlyMajorIndex(favoriteIds);

        return response;
    }

    /**
     * 차트조회
     */
    @Override
    public IndexChartDto getIndexChart(Long indexInfoId, ChartPeriodType periodType) {
        IndexChartDto indexChartDto = dashBoardRepository.findIndexChartData(indexInfoId, periodType);

        if (indexChartDto == null) {
            throw new EntityNotFoundException("Cannot find Index Chart with ID: " + indexInfoId);
        }

        return indexChartDto;
    }

    @Override
    public List<IndexDataRank> getIndexPerformance(String periodType, int limit) {
        List<IndexDataRank> response = new ArrayList<>();

        // indexData 테이블에 있는 모든 indexInfoId 조회
        List<Long> favoriteIds = indexDataRepository.findDistinctIndexInfoIds();

        List<MajorIndexDataResponse> majorDatalist = new ArrayList<>();

        if(periodType.equals("DAILY")) majorDatalist = dailyMajorIndex(favoriteIds);
        if(periodType.equals("WEEKLY")) majorDatalist = weeklyMajorIndex(favoriteIds);
        if(periodType.equals("MONTHLY")) majorDatalist = monthlyMajorIndex(favoriteIds);

        AtomicInteger counter = new AtomicInteger(1);

        response = majorDatalist.stream()
                .sorted(Comparator.comparing(MajorIndexDataResponse::currentPrice).reversed()) // 내림차순으로 정렬
                .map(indexData -> {
                    return IndexDataRank.builder()
                            .performance(indexData)
                            .rank(counter.getAndIncrement())
                            .build();
                }).toList();

        return response;
    }

    /**
     * 일 별 주요지수
     * - 오늘 날짜와 어제 날짜 반환
     * - 계산 필요없이 DB값 그대로, 어제 날짜만 찾으면 됨
     */
    private List<MajorIndexDataResponse> dailyMajorIndex(List<Long> favoriteIds) {

        // 1. 오늘의 지수 데이터 조회
        List<MajorIndexDto> latestMajorIndexList = favoriteIds.stream()
                .map(dashBoardRepository::getLatestMajorIndexData)
                .filter(Objects::nonNull)
                .toList();

        // 2. 어제의 지수 데이터 조회
        List<MajorIndexDto> beforeDayMajorIndexList = favoriteIds.stream()
                .map(dashBoardRepository::getBeforeDayMajorIndexData)
                .filter(Objects::nonNull)
                .toList();

        List<MajorIndexDataResponse> result = new ArrayList<>();

        // 어제 데이터 Map으로 변환 (key = indexInfoId)
        Map<Long, MajorIndexDto> beforeMap = beforeDayMajorIndexList.stream()
                .collect(Collectors.toMap(MajorIndexDto::indexInfoId, dto -> dto));

        // latest 중에서 before 데이터가 있는 경우만 계산
        for (MajorIndexDto latest : latestMajorIndexList) {
            MajorIndexDto before = beforeMap.get(latest.indexInfoId());
            if (before != null) {
                BigDecimal currentPrice = latest.closingPrice();
                BigDecimal beforePrice = before.closingPrice();

                // 등락/등락률은 DB에서 가져온 값 대신 직접 계산해도 됨
                BigDecimal versus = currentPrice.subtract(beforePrice);
                BigDecimal fluctuationRate = beforePrice.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : versus.divide(beforePrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                result.add(MajorIndexDataResponse.builder()
                        .indexInfoId(latest.indexInfoId())
                        .indexClassification(latest.indexClassification())
                        .indexName(latest.indexName())
                        .versus(versus)
                        .fluctuationRate(fluctuationRate)
                        .currentPrice(currentPrice)
                        .beforePrice(beforePrice)
                        .build());
            }
        }

        return result;
    }



    /**
     * 주 별 주요 지수
     */
    private List<MajorIndexDataResponse> weeklyMajorIndex(List<Long> favoriteIds) {
        // 1. 오늘의 지수 데이터 조회
        List<MajorIndexDto> latestMajorIndexList = favoriteIds.stream()
                .map(dashBoardRepository::getLatestMajorIndexData)
                .filter(Objects::nonNull)
                .toList();

        // 2. 일주일 전의 지수 데이터 조회
        List<MajorIndexDto> beforeWeekMajorIndexList = favoriteIds.stream()
                .map(dashBoardRepository::getBeforeWeekMajorIndexData)
                .filter(Objects::nonNull)
                .toList();

        List<MajorIndexDataResponse> result = new ArrayList<>();

        // before 데이터 Map으로 변환
        Map<Long, MajorIndexDto> beforeMap = beforeWeekMajorIndexList.stream()
                .collect(Collectors.toMap(MajorIndexDto::indexInfoId, dto -> dto));

        // latest 중에서 before 데이터가 있는 경우만 처리
        for (MajorIndexDto latest : latestMajorIndexList) {
            MajorIndexDto before = beforeMap.get(latest.indexInfoId());
            if (before != null) {
                BigDecimal currentPrice = latest.closingPrice();
                BigDecimal beforePrice = before.closingPrice();
                BigDecimal versus = currentPrice.subtract(beforePrice);
                BigDecimal fluctuationRate = beforePrice.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : versus.divide(beforePrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                result.add(MajorIndexDataResponse.builder()
                        .indexInfoId(latest.indexInfoId())
                        .indexClassification(latest.indexClassification())
                        .indexName(latest.indexName())
                        .versus(versus)
                        .fluctuationRate(fluctuationRate)
                        .currentPrice(currentPrice)
                        .beforePrice(beforePrice)
                        .build());
            }
        }

        return result;
    }


    /**
     * 월 별 주요 지수
     */
    private List<MajorIndexDataResponse> monthlyMajorIndex(List<Long> favoriteIds) {
        // 1. 오늘의 지수 데이터 조회
        List<MajorIndexDto> latestMajorIndexList = favoriteIds.stream()
                .map(dashBoardRepository::getLatestMajorIndexData)
                .filter(Objects::nonNull)
                .toList();

        // 2. 한 달 전의 지수 데이터
        List<MajorIndexDto> beforeMonthMajorIndexList = favoriteIds.stream()
                .map(dashBoardRepository::getBeforeMonthMajorIndexData)
                .filter(Objects::nonNull)
                .toList();

        List<MajorIndexDataResponse> result = new ArrayList<>(); // 없으면 빈 문자열 넘겨주기

        Map<Long, MajorIndexDto> beforeMap = beforeMonthMajorIndexList.stream()
                .collect(Collectors.toMap(MajorIndexDto::indexInfoId, dto -> dto));

        List<MajorIndexDto> existedLatestMajorIndex = latestMajorIndexList.stream()
                .filter(latest -> beforeMap.containsKey(latest.indexInfoId())).toList();

        for(MajorIndexDto latest : existedLatestMajorIndex) {
            for(MajorIndexDto before : beforeMap.values()) {
                if(latest.indexInfoId().equals(before.indexInfoId())) {
                    BigDecimal currentPrice = latest.closingPrice();
                    BigDecimal beforePrice = before.closingPrice();
                    BigDecimal versus = currentPrice.subtract(beforePrice);
                    BigDecimal fluctuationRate = beforePrice.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : versus.divide(beforePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

                    result.add(MajorIndexDataResponse.builder()
                            .indexInfoId(latest.indexInfoId())
                            .indexClassification(latest.indexClassification())
                            .indexName(latest.indexName())
                            .versus(versus)
                            .fluctuationRate(fluctuationRate)
                            .currentPrice(currentPrice)
                            .beforePrice(beforePrice)
                            .build());
                }
            }
        }
        return result;
    }


}
