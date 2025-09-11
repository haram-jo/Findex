package com.codeit.findex.repository;

import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.custom.SyncJobRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long>, SyncJobRepositoryCustom {
}
