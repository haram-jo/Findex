package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.entity.SyncJob;

import java.util.List;

public interface SyncJobRepositoryCustom {
    List<SyncJob> search(SyncJobSearchRequest param);
    long count(SyncJobSearchRequest param);
}
