package com.codeit.findex.schedular;

import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.service.basic.BasicSyncJobService;
import com.codeit.findex.service.schedular.AnchorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexDataAutoSyncScheduler {

    private final AutoSyncRepository autoSyncRepository;
    private final AnchorService anchorService;
    private final BasicSyncJobService basicSyncJobService; // ★ 기존 서비스 그대로 재사용

    // 필요시 application.yml 로 뺄 수 있음 (그러나 안뺄것임)
    private static final int BACKFILL_DAYS_WHEN_NO_ANCHOR = 7;
    private static final long API_DELAY_MS = 150L;

    /**
     * 매일 00:00 (KST) 실행
     */
//    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul") // 10초마다 (테스트용) 삭제 X
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void runDaily() {
        log.info("[IndexDataAutoSync] start");

        // 1) 활성 지수 추출
        List<Long> enabledIndexIds = autoSyncRepository.findEnabledIndexIds();
        log.info("[IndexDataAutoSync] enabled size = {}", enabledIndexIds.size()); // 활성화가 지금 몇명이냐?
        for (Long indexId : enabledIndexIds) {
            try {
                // 2) 마지막 자동 연동 작업 날짜(anchor) 조회
                LocalDate anchor = anchorService.findLastSuccessDate(indexId);

                // 3) 대상 날짜 범위 계산 (요구사항: 마지막 자동 연동 작업 날짜 ~ 최신)
                LocalDate start = (anchor != null)
                        ? anchor.plusDays(1)
                        : LocalDate.now().minusDays(BACKFILL_DAYS_WHEN_NO_ANCHOR);
                LocalDate end = LocalDate.now();

                if (start.isAfter(end)) {
                    log.info("[IndexDataAutoSync] skip indexId={} (no range)", indexId);
                    continue;
                }

                // 4) 기존 서비스 로직 재사용 (컨트롤러 안 거치고 서비스 직접 호출)
                IndexDataSyncRequest req = new IndexDataSyncRequest(
                        List.of(indexId),
                        start.toString(), // "yyyy-MM-dd"
                        end.toString()
                );
                basicSyncJobService.createIndexDataSyncJob("system", req);

                // 5) API rate-limit 배려
                try { Thread.sleep(API_DELAY_MS); } catch (InterruptedException ignored) {}

            } catch (Exception e) {
                log.error("[IndexDataAutoSync] indexId={} failed", indexId, e);
            }
        }

        log.info("[IndexDataAutoSync] end");
    }
}
