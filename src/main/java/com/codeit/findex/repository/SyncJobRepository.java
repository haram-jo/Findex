package com.codeit.findex.repository;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.custom.SyncJobRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long>, SyncJobRepositoryCustom {

    Optional<SyncJob> findTopByJobTypeOrderByJobTimeDesc(JobType jobType);

    boolean existsByJobTypeAndIndexInfoIdAndTargetDate(
            JobType jobType,
            Long indexInfoId,
            LocalDate targetDate
    );

    Optional<SyncJob> findByIndexInfoIdAndTargetDate(Long indexInfoId, LocalDate targetDate);

    List<SyncJob> findByJobType(JobType jobType);

    List<SyncJob> findByJobTypeAndIndexInfo_IdIn(JobType jobType, List<Long> indexInfoIds);

}
