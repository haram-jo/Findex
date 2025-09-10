package com.codeit.findex.service;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.response.MarketIndexApiResponse;

import java.util.List;

public interface SyncJobService {

    List<SyncJobDto> createSyncJob(String workerId);

    List<SyncJobDto> createIndexDataSyncJob(String workerId, IndexDataSyncRequest request);

    MarketIndexApiResponse findAll();

}
