package com.codeit.findex.service;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.response.MarketIndexApiResponse;

import java.util.List;

public interface SyncJobService {

    void createSyncJob(String workerId);
    public MarketIndexApiResponse findAll();
}
