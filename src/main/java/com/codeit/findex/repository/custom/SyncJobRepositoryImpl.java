package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.entity.ResultType;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.service.basic.BasicSyncJobService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SyncJobRepositoryImpl implements SyncJobRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<SyncJob> search(SyncJobSearchRequest param) {
        StringBuilder jpqlBuilder = new StringBuilder("SELECT s FROM SyncJob s WHERE 1=1");

        // 동적 조건
        if (param.jobType() != null) {
            jpqlBuilder.append(" AND s.jobType = :jobType");
        }
        if (param.indexInfoId() != null) {
            jpqlBuilder.append(" AND s.indexInfo.id = :indexInfoId");
        }
        if (param.baseDateFrom() != null) {
            jpqlBuilder.append(" AND s.targetDate >= :baseDateFrom");
        }
        if (param.baseDateTo() != null) {
            jpqlBuilder.append(" AND s.targetDate <= :baseDateTo");
        }
        if (param.worker() != null && !param.worker().isBlank()) {
            jpqlBuilder.append(" AND s.worker = :worker");
        }
        if (param.jobTimeFrom() != null) {
            jpqlBuilder.append(" AND s.jobTime >= :jobTimeFrom");
        }
        if (param.jobTimeTo() != null) {
            jpqlBuilder.append(" AND s.jobTime <= :jobTimeTo");
        }
        if (param.status() != null && !param.status().isBlank()) {
            jpqlBuilder.append(" AND s.result = :status");
        }
        if (param.idAfter() != null) {
            jpqlBuilder.append(" AND s.id > :idAfter");
        }

        // 정렬
        String sortField = param.sortField();
        String sortDirection = param.sortDirection().equalsIgnoreCase("asc") ? "ASC" : "DESC";
        jpqlBuilder.append(" ORDER BY s.").append(sortField).append(" ").append(sortDirection);

        // 쿼리 생성
        TypedQuery<SyncJob> query = em.createQuery(jpqlBuilder.toString(), SyncJob.class);

        // 파라미터 바인딩 (조건문과 반드시 동일하게 묶음)
        if (param.jobType() != null) {
            query.setParameter("jobType", param.jobType());
        }
        if (param.indexInfoId() != null) {
            query.setParameter("indexInfoId", param.indexInfoId());
        }
        if (param.baseDateFrom() != null) {
            query.setParameter("baseDateFrom", param.baseDateFrom());
        }
        if (param.baseDateTo() != null) {
            query.setParameter("baseDateTo", param.baseDateTo());
        }
        if (param.worker() != null && !param.worker().isBlank()) {
            query.setParameter("worker", param.worker());
        }
        if (param.jobTimeFrom() != null) {
            query.setParameter("jobTimeFrom", param.jobTimeFrom());
        }
        if (param.jobTimeTo() != null) {
            query.setParameter("jobTimeTo", param.jobTimeTo());
        }
        if (param.status() != null && !param.status().isBlank()) {
            query.setParameter("status", ResultType.valueOf(param.status().toUpperCase()).toBoolean());
        }
        if (param.idAfter() != null) {
            query.setParameter("idAfter", param.idAfter());
        }

        query.setMaxResults(param.size() + 1); // 페이지네이션 (다음 페이지 여부 확인용)
        return query.getResultList();
    }


    @Override
    public long count(SyncJobSearchRequest param) {

        StringBuilder jpqlBuilder = new StringBuilder("SELECT count(d) FROM SyncJob d WHERE 1=1");

        if(param.indexInfoId() != null) jpqlBuilder.append(" AND d.indexInfo.id = :indexInfoId");
        if(param.baseDateFrom() != null) jpqlBuilder.append(" AND d.targetDate >= :baseDateFrom");
        if(param.baseDateTo() != null) jpqlBuilder.append(" AND d.targetDate <= :baseDateTo");

        // 최종 쿼리
        TypedQuery<Long> query = em.createQuery(jpqlBuilder.toString(), Long.class);

        if(param.indexInfoId() != null) query.setParameter("indexInfoId", param.indexInfoId());
        if(param.baseDateFrom() != null) query.setParameter("baseDateFrom", param.baseDateFrom());
        if(param.baseDateTo() != null) query.setParameter("baseDateTo", param.baseDateTo());

        return query.getSingleResult();
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
