package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.dto.response.MajorIndexDataResponse;
import com.codeit.findex.entity.QIndexData;
import com.codeit.findex.entity.QIndexInfo;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashBoardRepositoryImpl implements DashBoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MajorIndexDto> getFavoriteMajorIndexData(String periodType) {
        QIndexData indexData = QIndexData.indexData;
        QIndexInfo indexInfo = QIndexInfo.indexInfo;

        // 최신 baseDate 가져오기 (기간 단위별로 다르게)
        Expression<?> groupExpr;
        switch (periodType.toUpperCase()) {
            case "WEEKLY" -> groupExpr = Expressions.dateTemplate(LocalDate.class, "date_trunc('week', {0})", indexData.baseDate);
            case "MONTHLY" -> groupExpr = Expressions.dateTemplate(LocalDate.class, "date_trunc('month', {0})", indexData.baseDate);
            default -> groupExpr = indexData.baseDate;
        }

        return queryFactory
                .select(Projections.constructor(MajorIndexDto.class,
                        indexInfo.id.as("indexInfoId"),
                        indexInfo.indexClassification,
                        indexInfo.indexName,
                        indexData.baseDate,
                        indexData.closingPrice
                ))
                .from(indexData)
                .join(indexData.indexInfo, indexInfo)
                .where(indexInfo.favorite.isTrue())
                .orderBy(indexData.baseDate.desc())
                .fetch();
    }

}
