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
    Optional<SyncJob> findTopByOrderByJobTimeDesc();

    Optional<SyncJob> findTopByJobTypeOrderByJobTimeDesc(JobType jobType);

    List<SyncJob> findByJobTypeAndIndexInfoIdAndTargetDateBetween(
            JobType jobType,
            Long indexInfoId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByJobTypeAndIndexInfoIdAndTargetDate(
            JobType jobType,
            Long indexInfoId,
            LocalDate targetDate
    );
}
