package com.codeit.findex.service;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.entity.IndexData;

import java.util.List;

public interface SyncJobService {

    // 지수 정보 연동
    List<SyncJobDto> createSyncJobsOfIndexInfo(String workerId);

    // 지수 데이터 연동
    List<SyncJobDto> createSyncJobsOfIndexData(String workerId, IndexDataSyncRequest request);

    // 연동 작업 목록 조회
    CursorPageResponseSyncJobDto findAll(SyncJobSearchRequest request);


}
