package com.codeit.findex.service.basic;

import com.codeit.findex.client.MarketIndexApiClient;
import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.entity.*;
import com.codeit.findex.mapper.SyncJobMapper;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.repository.SyncJobRepository;
import com.codeit.findex.service.SyncJobService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicSyncJobService implements SyncJobService {

    private final IndexDataRepository indexDataRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexInfoSyncService indexInfoSyncService;
    private final IndexDataSyncService indexDataSyncService;
    private final SyncJobRepository syncJobRepository;
    private final SyncJobMapper syncJobMapper;

    /**
     * 지수 정보
     * @param workerId 작성자의 IP 주소
     */
    @Override
    @Transactional
    public List<SyncJobDto> createIndexInfoSyncJob(String workerId) {
        // 1. 지수 정보 DB에 저장
        indexInfoSyncService.createIndexInfos();

        // 2. 연동 정보 DB에 저장
        List<SyncJobDto> syncJobDtos = createSyncJobsOfIndexInfo(workerId);

        return syncJobDtos;
    }

    private List<SyncJobDto> createSyncJobsOfIndexInfo(String workerId) {
        List<SyncJobDto> syncJobRegistry = new ArrayList<>(); // SyncJob 테이블에 최종적으로 저장되는 데이터 목록
        List<IndexInfo> syncedIndexInfos = indexInfoRepository.findAll();

        for (IndexInfo indexInfo : syncedIndexInfos) {
            SyncJobDto newSyncJob = SyncJobDto.builder()
                    .indexInfoId(indexInfo.getId())
                    .jobType(JobType.INDEX_INFO)
                    .jobTime(LocalDateTime.now()) // 작업 일시
                    .targetDate(LocalDate.now()) // 연동한 날짜(대상 날짜)
                    .worker(workerId)
                    .result(ResultType.SUCCESS)
                    .build();

            syncJobRegistry.add(newSyncJob);
        }

        if (!syncJobRegistry.isEmpty()) {
            syncJobRepository.saveAllInBatch(syncJobRegistry);
        }
        return syncJobRegistry;
    }

    @Override
    @Transactional
    public List<SyncJobDto> createIndexDataSyncJob(String workerId, IndexDataSyncRequest request) {
        // 1. 지수 데이터 DB에 저장
        indexDataSyncService.createIndexData(request);

        // 2. 연동 정보 DB에 저장
        List<SyncJobDto> syncJobDtos = createSyncJobsOfIndexData(workerId, request.indexInfoIds().get(0));

        return syncJobDtos;
    }

    private List<SyncJobDto> createSyncJobsOfIndexData(String workerId, Long indexInfoId) {
        List<SyncJobDto> syncJobRegistry = new ArrayList<>(); // SyncJob 테이블에 최종적으로 저장되는 데이터 목록
        List<IndexData> syncedIndexInfos = indexDataRepository.findAll();

        for (IndexData indexData : syncedIndexInfos) {
            SyncJobDto newSyncJob = SyncJobDto.builder()
                    .indexInfoId(indexInfoId)
                    .jobType(JobType.INDEX_DATA)
                    .jobTime(LocalDateTime.now()) // 작업 일시
                    .targetDate(indexData.getBaseDate()) // 연동한 날짜(대상 날짜)
                    .worker(workerId)
                    .result(ResultType.SUCCESS)
                    .build();

            syncJobRegistry.add(newSyncJob);
        }

        if (!syncJobRegistry.isEmpty()) {
            syncJobRepository.saveAllInBatchWithTargetDate(syncJobRegistry);
        }
        return syncJobRegistry;
    }

    @Override
    public CursorPageResponseSyncJobDto findAll(SyncJobSearchRequest param) {

        // 1. 쿼리로 데이터 조회
        List<SyncJob> syncJobList = syncJobRepository.search(param);
        long total = syncJobRepository.count(param);

        // 2. 다음 페이지 존재여부 확인
        boolean hasNext = syncJobList.size() > param.size();
        if (hasNext) syncJobList.remove(syncJobList.size() - 1);

        List<SyncJobDto> content = syncJobList.stream().map(syncJobMapper::toDto).toList();

        String nextCursor = null;
        Long nextIdAfter = null;

        if(hasNext) {
            SyncJob lastItem = syncJobList.get(syncJobList.size() - 1);
            String cursorJson = String.format("{\"id\":%d}", lastItem.getId());
            nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
            nextIdAfter = lastItem.getId();
        }

        return CursorPageResponseSyncJobDto.builder()
                .content(content)
                .nextCursor(nextCursor)
                .nextIdAfter(nextIdAfter)
                .size(param.size())
                .totalElements(total)
                .hasNext(hasNext)
                .build();
    }
}