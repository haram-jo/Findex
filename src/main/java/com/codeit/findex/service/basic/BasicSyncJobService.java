package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.SyncJob;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicSyncJobService implements SyncJobService {

    private final IndexInfoRepository indexInfoRepository;
    private final SyncJobRepository syncJobRepository;
    private final SyncJobMapper syncJobMapper;
    private final IndexDataRepository indexDataRepository;


    @Override
    @Transactional
    public List<SyncJobDto> createSyncJobsOfIndexInfo(String workerId) {
        List<IndexInfo> syncedIndexInfos = indexInfoRepository.findAll();
        List<SyncJob> existingJobs = syncJobRepository.findByJobType(JobType.INDEX_INFO);

        // 기존 job을 indexInfo.id 기준으로 Map 변환
        Map<Long, SyncJob> existingMap = existingJobs.stream()
                .collect(Collectors.toMap(job -> job.getIndexInfo().getId(), job -> job));

        List<SyncJob> jobsToSave = new ArrayList<>();

        for (IndexInfo indexInfo : syncedIndexInfos) {
            SyncJob existing = existingMap.get(indexInfo.getId());

            if (existing != null) {
                // 기존 엔티티 업데이트 (Dirty Checking)
                existing.update(LocalDateTime.now(), workerId, true);
                jobsToSave.add(existing);
            } else {
                // 새 엔티티 생성
                SyncJob newJob = SyncJob.builder()
                        .indexInfo(indexInfo)
                        .jobType(JobType.INDEX_INFO)
                        .jobTime(LocalDateTime.now())
                        .targetDate(LocalDate.now())
                        .worker(workerId)
                        .result(true)
                        .build();
                jobsToSave.add(newJob);
            }
        }

        // bulk save (insert + update)
        List<SyncJob> saved = syncJobRepository.saveAll(jobsToSave);

        // DTO 변환
        return saved.stream()
                .map(syncJobMapper::toDto)
                .toList();
    }


    @Override
    @Transactional
    public List<SyncJobDto> createSyncJobsOfIndexData(String workerId, IndexDataSyncRequest request) {

        // 1. IndexData 조회
        List<IndexData> indexDataLists = indexDataRepository.findByIndexInfo_IdIn(request.indexInfoIds());

        // 2. 기존 SyncJob 조회
        List<SyncJob> existingJobs = syncJobRepository.findByJobTypeAndIndexInfo_IdIn(
                JobType.INDEX_DATA, request.indexInfoIds()
        );

        List<SyncJob> jobsToSave = new ArrayList<>();

        // 3. 기존 job을 indexInfo.id 기준으로 Map 변환
        Map<Long, SyncJob> existingMap = existingJobs.stream()
                .collect(Collectors.toMap(job -> job.getIndexInfo().getId(), job -> job, (a, b) -> a));

        // 4. 신규/기존 job 처리
        for (IndexData indexData : indexDataLists) {
            Long indexInfoId = indexData.getIndexInfo().getId();
            SyncJob existing = existingMap.get(indexInfoId);

            if (existing != null) {
                existing.update(LocalDateTime.now(), workerId, true);
                jobsToSave.add(existing);
            } else {
                SyncJob newJob = SyncJob.builder()
                        .indexInfo(indexData.getIndexInfo())     // 여기서 굳이 다시 findById 할 필요 없음
                        .jobType(JobType.INDEX_DATA)
                        .targetDate(indexData.getBaseDate())     // LocalDate.now() 대신 baseDate가 자연스러움
                        .worker(workerId)
                        .jobTime(LocalDateTime.now())
                        .result(true)
                        .build();
                jobsToSave.add(newJob);
            }
        }

        // 5. bulk save (insert + update)
        List<SyncJob> saved = syncJobRepository.saveAll(jobsToSave);

        // 6. DTO 변환
        return saved.stream()
                .map(syncJobMapper::toDto)
                .toList();
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