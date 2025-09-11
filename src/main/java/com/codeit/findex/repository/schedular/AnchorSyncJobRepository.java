package com.codeit.findex.repository.schedular;

import com.codeit.findex.service.basic.BasicSyncJobService;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnchorSyncJobRepository extends JpaRepository<SyncJob, Long> {

    Optional<SyncJob> findTopByIndexInfo_IdAndJobTypeAndWorkerAndResultOrderByTargetDateDesc(
            Long indexInfoId,
            JobType jobType,
            String worker,
            BasicSyncJobService.ResultType result
    );
}

