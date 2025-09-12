package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.entity.QIndexData;
import com.codeit.findex.entity.QIndexInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashBoardRepositoryImpl implements DashBoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MajorIndexDto> getFavoriteMajorIndexData(int month) {
        QIndexInfo indexInfo = QIndexInfo.indexInfo;
        QIndexData indexData = QIndexData.indexData;

        int currentMonth = month;
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
                                "EXTRACT(MONTH FROM {0})", indexData.baseDate).in(currentMonth, beforeMonth))
                )
                .orderBy(indexInfo.id.asc(), indexData.baseDate.asc())
                .fetch();
    }
}
