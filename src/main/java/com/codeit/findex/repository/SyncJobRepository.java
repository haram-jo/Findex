package com.codeit.findex.repository;

import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.custom.SyncJobRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long>, SyncJobRepositoryCustom {
    Optional<SyncJob> findTopByOrderByJobTimeDesc();

    Optional<SyncJob> findTopByJobTypeOrderByJobTimeDesc(JobType jobType);

}
