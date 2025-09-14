package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.entity.SyncJob;

import java.util.List;

public interface SyncJobRepositoryCustom {
    List<SyncJob> search(SyncJobSearchRequest param);
    long count(SyncJobSearchRequest param);

    void saveAllInBatch(List<SyncJobDto> syncJobs);
    void saveAllInBatchWithTargetDate(List<SyncJobDto> syncJobs);
}
