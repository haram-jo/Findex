package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.dto.response.MajorIndexDataResponse;
import com.codeit.findex.repository.DashBoardRepository;
import com.codeit.findex.service.DashBoardService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicDashBoardService implements DashBoardService {

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

        List<MajorIndexDto> rawData = dashBoardRepository.getFavoriteMajorIndexData(month);

        List<MajorIndexDataResponse> response = null;

        if(periodType.equals("DAILY")) response = dailyMajorIndex(rawData);
        if(periodType.equals("WEEKLY")) response = weeklyMajorIndex(rawData);
        if(periodType.equals("MONTHLY")) response = monthlyMajorIndex(rawData);

        return response;
    }

    /**
     * 일 별 주요지수
     * - 오늘 날짜와 어제 날짜 반환
     * - 계산 필요없이 DB값 그대로, 어제 날짜만 찾으면 됨
     */
    private List<MajorIndexDataResponse> dailyMajorIndex(List<MajorIndexDto> data) {

        LocalDate today = LocalDate.now().minusDays(1); // 오늘 날짜(지수상 오늘 날짜는 없기 때문에 -1이 당일이라고 침

        // 오늘의 지수 데이터 조회
        List<MajorIndexDto> todayMajorIndexList = data.stream()
                .filter(indexData -> indexData.baseDate().equals(today)).toList();

        List<MajorIndexDataResponse> result = new ArrayList<>(); // 없으면 빈 문자열 넘겨주기

        if(!todayMajorIndexList.isEmpty()) {
            result =  todayMajorIndexList.stream().map(majorIndex -> {

                Optional<MajorIndexDto> yesterdayData = data.stream()
                        .filter(d -> d.indexInfoId().equals(majorIndex.indexInfoId()))
                        .filter(d -> d.baseDate().isBefore(today))
                        .max(Comparator.comparing(MajorIndexDto::baseDate));

                BigDecimal currentPrice = majorIndex.closingPrice();
                BigDecimal beforePrice = yesterdayData.map(MajorIndexDto::closingPrice).orElse(currentPrice); // 전날 값이 없으면 현재 값으로 채움
                BigDecimal versus = majorIndex.versus(); // 일별 날짜는 DB에서 내려주는 값 그대로 계산 필요 X
                BigDecimal fluctuationRate = majorIndex.fluctuationRate(); // 일별 날짜는 DB에서 내려주는 값 그대로 계산 필요 X

                return MajorIndexDataResponse.builder()
                        .indexInfoId(majorIndex.indexInfoId())
                        .indexClassification(majorIndex.indexClassification())
                        .indexName(majorIndex.indexName())
                        .versus(versus)
                        .fluctuationRate(fluctuationRate)
                        .currentPrice(currentPrice)
                        .beforePrice(beforePrice)
                        .build();
            }).toList();
        }

        return result;
    }


    /**
     * 주 별 주요 지수
     */
    private List<MajorIndexDataResponse> weeklyMajorIndex(List<MajorIndexDto> data) {

        List<MajorIndexDataResponse> result = new ArrayList<>();

        LocalDate thisWeek = LocalDate.now().minusDays(1); // 이번주
        LocalDate beforeWeek = thisWeek.minusWeeks(1).with(DayOfWeek.FRIDAY); // 지난주 금요일

        // 이번주 지수 데이터
        List<MajorIndexDto> thisWeekIndexList = data.stream()
                .filter(indexData -> indexData.baseDate().equals(thisWeek)).toList();

        // 지난 주 지수 데이터
        List<MajorIndexDto> beforeWeekIndexList = data.stream()
                .filter(indexData -> indexData.baseDate().equals(beforeWeek)).toList();

        if(!thisWeekIndexList.isEmpty() && !beforeWeekIndexList.isEmpty()) {
                result = thisWeekIndexList.stream().map(majorIndex -> {
                    BigDecimal currentPrice = majorIndex.closingPrice(); // 이번 주
                    BigDecimal beforePrice = beforeWeekIndexList.stream()
                            .filter(beforeData -> beforeData.indexInfoId().equals(majorIndex.indexInfoId()))
                            .map(MajorIndexDto::closingPrice).findFirst().orElse(currentPrice);
                    BigDecimal versus = currentPrice.subtract(beforePrice);
                    BigDecimal fluctuationRate = beforePrice.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : versus.divide(beforePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

                    return MajorIndexDataResponse.builder()
                            .indexInfoId(majorIndex.indexInfoId())
                            .indexClassification(majorIndex.indexClassification())
                            .indexName(majorIndex.indexName())
                            .versus(versus)
                            .fluctuationRate(fluctuationRate)
                            .currentPrice(currentPrice)
                            .beforePrice(beforePrice)
                            .build();
                }).toList();
        }

        return result;
    }

    /**
     * 월 별 주요 지수
     */
    private List<MajorIndexDataResponse> monthlyMajorIndex(List<MajorIndexDto> data) {
        List<MajorIndexDataResponse> result = new ArrayList<>();

        LocalDate thisMonth = LocalDate.now().minusDays(1);
        LocalDate beforeMonth = LocalDate.now().minusMonths(1); // 8예상

        // 이번 달 지수 데이터
        List<MajorIndexDto> thisMonthData = data.stream()
                .filter(indexData -> indexData.baseDate().equals(thisMonth)).toList();

        // 지난 달 지수 데이터(지난달의 가장 최신 데이터)
        List<MajorIndexDto> beforeMonthData = data.stream()
                // 지난 달 데이터만 필터링
                .filter(indexData -> indexData.baseDate().getMonthValue() == beforeMonth.getMonthValue())
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            if (list.isEmpty()) return List.of(); // 빈 경우 방어
                            LocalDate latestDate = list.stream()
                                    .map(MajorIndexDto::baseDate)
                                    .max(LocalDate::compareTo)
                                    .get();

                            return list.stream()
                                    .filter(item -> item.baseDate().equals(latestDate))
                                    .toList();
                        }
                ));

        if(!thisMonthData.isEmpty() && !beforeMonthData.isEmpty()) {
            result = thisMonthData.stream().map(majorIndex -> {
                BigDecimal currentPrice = majorIndex.closingPrice();
                BigDecimal beforePrice = beforeMonthData.stream()
                        .filter(beforeData -> beforeData.indexInfoId().equals(majorIndex.indexInfoId()))
                        .map(MajorIndexDto::closingPrice).findFirst().orElse(currentPrice);
                BigDecimal versus = currentPrice.subtract(beforePrice);
                BigDecimal fluctuationRate = beforePrice.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : versus.divide(beforePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

                return MajorIndexDataResponse.builder()
                        .indexInfoId(majorIndex.indexInfoId())
                        .indexClassification(majorIndex.indexClassification())
                        .indexName(majorIndex.indexName())
                        .versus(versus)
                        .fluctuationRate(fluctuationRate)
                        .currentPrice(currentPrice)
                        .beforePrice(beforePrice)
                        .build();
            }).toList();

        }
        return result;
    }






}
