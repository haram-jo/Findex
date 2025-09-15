package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.entity.QSyncJob;
import com.codeit.findex.entity.SyncJob;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SyncJobRepositoryImpl implements SyncJobRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    @Override
    public List<SyncJob> search(SyncJobSearchRequest param) {

        QSyncJob syncJob = QSyncJob.syncJob;

        // 1. where 조건 빌드
        BooleanBuilder where = new BooleanBuilder();

        if (param.jobType() != null) where.and(syncJob.jobType.eq(param.jobType()));
        if (param.indexInfoId() != null) where.and(syncJob.indexInfo.id.eq(param.indexInfoId()));
        if (param.baseDateFrom() != null) where.and(syncJob.targetDate.goe(param.baseDateFrom()));
        if (param.baseDateTo() != null) where.and(syncJob.targetDate.loe(param.baseDateTo()));
        if (param.worker() != null && !param.worker().trim().isBlank()) where.and(syncJob.worker.eq(param.worker()));
        if (param.jobTimeFrom() != null) where.and(syncJob.jobTime.goe(LocalDateTime.from(param.jobTimeFrom())));
        if (param.jobTimeTo() != null) where.and(syncJob.jobTime.loe(LocalDateTime.from(param.jobTimeTo())));
        if (param.status() != null && !param.status().trim().isBlank()) where.and(syncJob.result.eq(Boolean.valueOf(param.status())));
        if (param.idAfter() != null) where.and(syncJob.id.gt(param.idAfter()));

        Order order = "desc".equalsIgnoreCase(param.sortDirection()) ? Order.DESC : Order.ASC;
        OrderSpecifier<?> orderSpecifier;

        switch (param.sortDirection()) {
            case "targetDate" -> orderSpecifier = new OrderSpecifier<>(order, syncJob.targetDate);
            case "jobTime" -> orderSpecifier = new OrderSpecifier<>(order, syncJob.jobTime);
            default -> orderSpecifier = new OrderSpecifier<>(Order.ASC, syncJob.jobTime); // fallback
        }

        int limit = (param.size() != null ? param.size() : 10) + 1;

        return queryFactory.selectFrom(syncJob)
                .where(where)
                .orderBy(orderSpecifier)
                .limit(limit)
                .fetch();
    }



    @Override
    public long count(SyncJobSearchRequest param) {
        QSyncJob syncJob = QSyncJob.syncJob;

        BooleanBuilder where = new BooleanBuilder();

        if (param.indexInfoId() != null) {
            where.and(syncJob.indexInfo.id.eq(param.indexInfoId()));
        }

        if (param.baseDateFrom() != null && param.baseDateTo() != null) {
            // 두 값이 모두 있으면 between
            where.and(syncJob.targetDate.between(param.baseDateFrom(), param.baseDateTo()));
        } else if (param.baseDateFrom() != null) {
            where.and(syncJob.targetDate.goe(param.baseDateFrom()));
        } else if (param.baseDateTo() != null) {
            where.and(syncJob.targetDate.loe(param.baseDateTo()));
        }

        return Optional.ofNullable(queryFactory
                .select(syncJob.id.countDistinct())
                .from(syncJob)
                .where(where)
                .fetchOne()).orElse(0L);
    }


    @Override
    public void saveAllInBatch(List<SyncJobDto> syncJobs) {
        StringBuilder query = new StringBuilder();

        query.append("INSERT INTO sync_jobs ")
                .append("(index_info_id, job_type, worker, job_time, result) ")
                .append("VALUES ");

        for (int i = 0; i < syncJobs.size(); i++) {
            SyncJobDto jobs = syncJobs.get(i);

            query.append("(")
                    .append("'").append(jobs.indexInfoId()).append("', ")
                    .append("'").append(jobs.jobType()).append("', ")
                    .append("'").append(jobs.worker()).append("', ")
                    .append("'").append(jobs.jobTime()).append("', ")
                    .append(jobs.result().toBoolean())
                    .append(")");

            if (i < syncJobs.size() - 1) {
                query.append(", ");
            }
        }

        em.createNativeQuery(query.toString()).executeUpdate();
    }

    @Override
    public void saveAllInBatchWithTargetDate(List<SyncJobDto> syncJobs) {
        StringBuilder query = new StringBuilder();

        query.append("INSERT INTO sync_jobs ")
                .append("(index_info_id, target_date, job_type, worker, job_time, result) ")
                .append("VALUES ");

        for (int i = 0; i < syncJobs.size(); i++) {
            SyncJobDto jobs = syncJobs.get(i);

            query.append("(")
                    .append("'").append(jobs.indexInfoId()).append("', ")
                    .append("'").append(jobs.targetDate()).append("', ")
                    .append("'").append(jobs.jobType()).append("', ")
                    .append("'").append(jobs.worker()).append("', ")
                    .append("'").append(jobs.jobTime()).append("', ")
                    .append(jobs.result().toBoolean())
                    .append(")");

            if (i < syncJobs.size() - 1) {
                query.append(", ");
            }
        }

        em.createNativeQuery(query.toString()).executeUpdate();
    }
}
