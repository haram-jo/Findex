package com.codeit.findex.repository;

import com.codeit.findex.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {
}
