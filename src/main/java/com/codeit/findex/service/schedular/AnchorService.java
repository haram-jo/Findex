package com.codeit.findex.service.schedular;


import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.schedular.AnchorSyncJobRepository;
import com.codeit.findex.service.basic.BasicSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnchorService {

    private final AnchorSyncJobRepository repo;

    public LocalDate findLastSuccessDate(Long indexId) {
        Optional<SyncJob> last = repo.findTopByIndexInfo_IdAndJobTypeAndWorkerAndResultOrderByTargetDateDesc(
                indexId, JobType.INDEX_DATA, "system", BasicSyncJobService.ResultType.SUCCESS
        );
        return last.map(SyncJob::getTargetDate).orElse(null);
    }
}
