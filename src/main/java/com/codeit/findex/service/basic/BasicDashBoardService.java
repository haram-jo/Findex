package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.ChartPeriodType;
import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.dto.response.IndexDataRank;
import com.codeit.findex.dto.response.MajorIndexDataResponse;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.repository.DashBoardRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.DashBoardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicDashBoardService implements DashBoardService {

    private final IndexInfoRepository indexInfoRepository;
    private final DashBoardRepository dashBoardRepository;

    /**
     * 주요 지수
     * - 즐겨찾기한 지수들의 등락률 대비를 가져와서 계산
     * @param periodType DAILY, WEEKLY, MONTHLY
     */
    @Override
    public List<MajorIndexDataResponse> getMajorIndex(String periodType) {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();

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

        LocalDate now = LocalDate.now(); // 오늘
        int month = now.getMonthValue(); // 저번 달

        // DB에서 이번달과 저번달 데이터 모두 가져오기
        List<MajorIndexDto> rawData = dashBoardRepository.getCurrentAndPreviousMonthData(month);

        List<Long> favoriteIds = indexInfoRepository.findByFavoriteTrue()
                .stream()
                .map(IndexInfo::getId)
                .toList();

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
        List<MajorIndexDto> latestMajorIndexList = favoriteIds.stream().map(dashBoardRepository::getLatestMajorIndexData).toList();
        // 2. 어제의 지수 데이터
        List<MajorIndexDto> beforeDayMajorIndexList = favoriteIds.stream().map(dashBoardRepository::getBeforeDayMajorIndexData).toList();

        List<MajorIndexDataResponse> result = new ArrayList<>(); // 없으면 빈 문자열 넘겨주기

        if(!latestMajorIndexList.isEmpty() && !beforeDayMajorIndexList.isEmpty()) {
            for (int i = 0; i < latestMajorIndexList.size(); i++) {
                MajorIndexDto latest = latestMajorIndexList.get(i);
                MajorIndexDto before = beforeDayMajorIndexList.get(i);

                BigDecimal currentPrice = latest.closingPrice();
                BigDecimal beforePrice = before.closingPrice();
                BigDecimal versus = latest.versus();
                BigDecimal fluctuationRate = latest.fluctuationRate();

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
        List<MajorIndexDto> latestMajorIndexList = favoriteIds.stream().map(dashBoardRepository::getLatestMajorIndexData).toList();
        // 2. 일주일 전의 지수 데이터
        List<MajorIndexDto> beforeWeekMajorIndexList = favoriteIds.stream().map(dashBoardRepository::getBeforeWeekMajorIndexData).toList();

        List<MajorIndexDataResponse> result = new ArrayList<>(); // 없으면 빈 문자열 넘겨주기
        if(!latestMajorIndexList.isEmpty() && !beforeWeekMajorIndexList.isEmpty()) {
            for (int i = 0; i < latestMajorIndexList.size(); i++) {
                MajorIndexDto latest = latestMajorIndexList.get(i);
                MajorIndexDto before = beforeWeekMajorIndexList.get(i);

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

        return result;
    }

    /**
     * 월 별 주요 지수
     */
    private List<MajorIndexDataResponse> monthlyMajorIndex(List<Long> favoriteIds) {
        // 1. 오늘의 지수 데이터 조회
        List<MajorIndexDto> latestMajorIndexList = favoriteIds.stream().map(dashBoardRepository::getLatestMajorIndexData).toList();
        // 2. 한 달 전의 지수 데이터
        List<MajorIndexDto> beforeMonthMajorIndexList = favoriteIds.stream().map(dashBoardRepository::getBeforeMonthMajorIndexData).toList();

        List<MajorIndexDataResponse> result = new ArrayList<>(); // 없으면 빈 문자열 넘겨주기
        if(!latestMajorIndexList.isEmpty() && !beforeMonthMajorIndexList.isEmpty()) {
            for (int i = 0; i < latestMajorIndexList.size(); i++) {
                MajorIndexDto latest = latestMajorIndexList.get(i);
                MajorIndexDto before = beforeMonthMajorIndexList.get(i);

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

        return result;
    }


}
