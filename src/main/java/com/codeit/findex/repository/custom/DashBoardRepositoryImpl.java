package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.entity.QIndexData;
import com.codeit.findex.entity.QIndexInfo;
import static com.codeit.findex.entity.QIndexData.indexData;

import com.codeit.findex.dto.data.ChartDataPoint;
import com.codeit.findex.dto.data.ChartDataRow;
import com.codeit.findex.dto.data.ChartPeriodType;
import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.repository.IndexInfoRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashBoardRepositoryImpl implements DashBoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final IndexInfoRepository indexInfoRepository;

    @Override
    public IndexChartDto findIndexChartData(Long indexInfoId, ChartPeriodType periodType) {
        // 1. 지수 정보를 먼저 조회
        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId).orElse(null);
        if (indexInfo == null) {
            return null; // 서비스 레이어에서 Exception 처리
        }

        LocalDate startDate = calculateStartDate(periodType);
        LocalDate dataStartDate = startDate.minusDays(20);

        NumberTemplate<BigDecimal> ma5 = Expressions.numberTemplate(BigDecimal.class,
                "AVG({0}) OVER (ORDER BY {1} ROWS BETWEEN 4 PRECEDING AND CURRENT ROW)",
                indexData.closingPrice, indexData.baseDate);

        NumberTemplate<BigDecimal> ma20 = Expressions.numberTemplate(BigDecimal.class,
                "AVG({0}) OVER (ORDER BY {1} ROWS BETWEEN 19 PRECEDING AND CURRENT ROW)",
                indexData.closingPrice, indexData.baseDate);

        // 2. DB에서 데이터를 리스트로 조회
        List<ChartDataRow> rawData = queryFactory
                .select(Projections.constructor(ChartDataRow.class,
                        indexData.baseDate,
                        indexData.closingPrice,
                        ma5,
                        ma20
                ))
                .from(indexData)
                .where(indexData.indexInfo.id.eq(indexInfoId)
                        .and(indexData.baseDate.goe(dataStartDate)))
                .orderBy(indexData.baseDate.asc())
                .fetch();

        // 3. Java에서 DTO로 조립
        List<ChartDataPoint> dataPoints = new ArrayList<>();
        List<ChartDataPoint> ma5DataPoints = new ArrayList<>();
        List<ChartDataPoint> ma20DataPoints = new ArrayList<>();

        for (ChartDataRow row : rawData) {
            if (!row.date().isBefore(startDate)) {
                dataPoints.add(new ChartDataPoint(row.date(), row.value()));
                if (row.ma5() != null) {
                    ma5DataPoints.add(new ChartDataPoint(row.date(), row.ma5()));
                }
                if (row.ma20() != null) {
                    ma20DataPoints.add(new ChartDataPoint(row.date(), row.ma20()));
                }
            }
        }

        return new IndexChartDto(
                indexInfo.getId(),
                indexInfo.getIndexClassification(),
                indexInfo.getIndexName(),
                periodType,
                dataPoints,
                ma5DataPoints,
                ma20DataPoints
        );
    }

    @Override
    public List<MajorIndexDto> getFavoriteMajorIndexData(int month) {
        QIndexInfo indexInfo = QIndexInfo.indexInfo;
        QIndexData indexData = QIndexData.indexData;

        int beforeMonth = month -1;

        return queryFactory
                .select(Projections.constructor(MajorIndexDto.class,
                        indexInfo.id.as("indexInfoId"),
                        indexInfo.indexClassification,
                        indexInfo.indexName,
                        indexData.baseDate,
                        indexData.versus,
                        indexData.fluctuationRate,
                        indexData.closingPrice
                ))
                .from(indexInfo)
                .join(indexData).on(indexInfo.id.eq(indexData.indexInfo.id))
                .where(indexInfo.favorite.isTrue()
                        .and(Expressions.numberTemplate(Integer.class,
                                "EXTRACT(MONTH FROM {0})", indexData.baseDate).in(month, beforeMonth))
                )
                .orderBy(indexInfo.id.asc(), indexData.baseDate.asc())
                .fetch();
    }

    @Override
    public List<MajorIndexDto> getCurrentAndPreviousMonthData(int month) {
        QIndexInfo indexInfo = QIndexInfo.indexInfo;
        QIndexData indexData = QIndexData.indexData;

        int beforeMonth = month -1;

        return queryFactory
                .select(Projections.constructor(MajorIndexDto.class,
                        indexInfo.id.as("indexInfoId"),
                        indexInfo.indexClassification,
                        indexInfo.indexName,
                        indexData.baseDate,
                        indexData.versus,
                        indexData.fluctuationRate,
                        indexData.closingPrice
                ))
                .from(indexInfo)
                .join(indexData).on(indexInfo.id.eq(indexData.indexInfo.id))
                .where(Expressions.numberTemplate(Integer.class,
                                "EXTRACT(MONTH FROM {0})", indexData.baseDate).in(month, beforeMonth)
                )
                .orderBy(indexInfo.id.asc(), indexData.baseDate.asc())
                .fetch();
    }

    private LocalDate calculateStartDate(ChartPeriodType periodType) {
        LocalDate today = LocalDate.now();
        return switch (periodType) {
            case MONTHLY -> today.minusMonths(1);
            case QUARTERLY -> today.minusMonths(3);
            case YEARLY -> today.minusYears(1);
        };
    }

    @Override
    public MajorIndexDto getLatestMajorIndexData(Long indexInfoId) {
        QIndexData d = QIndexData.indexData;
        QIndexInfo i = QIndexInfo.indexInfo;

        return queryFactory
                .select(Projections.constructor(
                        MajorIndexDto.class,
                        d.indexInfo.id,          // IndexInfoId
                        i.indexClassification,   // 분류명
                        i.indexName,             // 지수명
                        d.baseDate,              // 기준일
                        d.versus,                // 대비
                        d.fluctuationRate,       // 등락률
                        d.closingPrice           // 종가
                ))
                .from(d)
                .join(d.indexInfo, i) // d.indexInfo (연관관계 필드)와 i 매핑
                .where(d.indexInfo.id.eq(indexInfoId))
                .orderBy(d.baseDate.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public MajorIndexDto getBeforeDayMajorIndexData(Long indexInfoId) {
        QIndexData d = QIndexData.indexData;
        QIndexInfo i = QIndexInfo.indexInfo;

        return queryFactory
                .select(Projections.constructor(
                        MajorIndexDto.class,
                        d.indexInfo.id,        // d.INDEX_INFO_ID
                        i.indexClassification, // i.INDEX_CLASSIFICATION
                        i.indexName,           // i.INDEX_NAME
                        d.baseDate,            // d.BASE_DATE
                        d.versus,              // d.VERSUS
                        d.fluctuationRate,     // d.FLUCTUATION_RATE
                        d.closingPrice         // d.CLOSING_PRICE
                ))
                .from(d)
                .join(i).on(d.indexInfo.id.eq(i.id))
                .where(d.indexInfo.id.eq(indexInfoId))
                .orderBy(d.baseDate.desc())
                .offset(1)  // OFFSET 1
                .limit(1)   // LIMIT 1
                .fetchOne();
    }

    @Override
    public MajorIndexDto getBeforeWeekMajorIndexData(Long indexInfoId) {
        QIndexData d = QIndexData.indexData;
        QIndexInfo i = QIndexInfo.indexInfo;

        return queryFactory
                .select(Projections.constructor(
                        MajorIndexDto.class,
                        d.indexInfo.id,        // d.INDEX_INFO_ID
                        i.indexClassification, // i.INDEX_CLASSIFICATION
                        i.indexName,           // i.INDEX_NAME
                        d.baseDate,            // d.BASE_DATE
                        d.versus,              // d.VERSUS
                        d.fluctuationRate,     // d.FLUCTUATION_RATE
                        d.closingPrice         // d.CLOSING_PRICE
                ))
                .from(d)
                .join(i).on(d.indexInfo.id.eq(i.id))
                .where(d.indexInfo.id.eq(indexInfoId))
                .orderBy(d.baseDate.desc())
                .offset(7)  // OFFSET 1
                .limit(1)   // LIMIT 1
                .fetchOne();
    }

    @Override
    public MajorIndexDto getBeforeMonthMajorIndexData(Long indexInfoId) {
        QIndexData d = QIndexData.indexData;
        QIndexInfo i = QIndexInfo.indexInfo;

        return queryFactory
                .select(Projections.constructor(
                        MajorIndexDto.class,
                        d.indexInfo.id,        // d.INDEX_INFO_ID
                        i.indexClassification, // i.INDEX_CLASSIFICATION
                        i.indexName,           // i.INDEX_NAME
                        d.baseDate,            // d.BASE_DATE
                        d.versus,              // d.VERSUS
                        d.fluctuationRate,     // d.FLUCTUATION_RATE
                        d.closingPrice         // d.CLOSING_PRICE
                ))
                .from(d)
                .join(i).on(d.indexInfo.id.eq(i.id))
                .where(d.indexInfo.id.eq(indexInfoId))
                .orderBy(d.baseDate.desc())
                .offset(30)  // OFFSET 1
                .limit(1)   // LIMIT 1
                .fetchOne();
    }

}
