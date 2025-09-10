package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.repository.SyncJobRepository;
import com.codeit.findex.service.SyncJobService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class BasicSyncJobService implements SyncJobService {

    @Value("${external.finance.service-key}")
    private String serviceKey;

    private final WebClient financeWebClient;
    private final IndexInfoRepository indexInfoRepository;
    private final SyncJobRepository syncJobRepository;

    @Transactional
    @Override
    public void createSyncJob(String workerId) {
        // 1. 지수 정보 DB에 저장
        createIndexInfos();

        List<SyncJob> syncJobList = indexInfoRepository.findAll().stream()
                .map(indexInfo -> {
                    return SyncJob.builder()
                            .indexInfo(indexInfo)
                            .jobType(JobType.INDEX_INFO)
                            .jobTime(Instant.now()) // 작업 일시
                            .targetDate(LocalDate.now()) // 연동한 날짜(대상 날짜)
                            .worker(workerId)
                            .result(true)
                            .build();
                }).toList();

        syncJobRepository.saveAll(syncJobList);

    }

    /** OpenApi에서 받아온 데이터로 Index_infos 값에 매핑 후 DB에 저장 */
    public void createIndexInfos() {
        int pageNo = 1;
        int pageSize = 100;

        Set<String> seen = new HashSet<>();

        // 1. OpenAPI 호출
        while (true) {
            // 데이터 1000개
            List<IndexInfo> newIndexInfoList = getOpenApiData(pageNo, pageSize).getResponse().getBody().getItems().getItem().stream()
                    .filter(item -> seen.add(item.getIndexClassification() + ":" + item.getIndexName())) // 지수분류명 + 지수명으로 중복 제거
                    // DB에 이미 존재하는 지수 정보는 제외
                    .filter(item -> !indexInfoRepository.existsByIndexClassificationAndIndexName(
                            item.getIndexClassification(),
                            item.getIndexName()
                    ))
                    .map(item -> IndexInfo.builder()
                            .indexClassification(item.getIndexClassification()) // 지수 분류 명
                            .indexName(item.getIndexName()) // 지수명
                            .employedItemsCount(Integer.valueOf(item.getEmployedItemsCount())) //채용 종목 수
                            .basePointInTime(item.getBasePointInTime())
                            .baseIndex(Double.valueOf(item.getBaseIndex()))
                            .sourceType(SourceType.OPEN_API)
                            .favorite(false)
                            .build()
                    )
                    .toList();

            // 중복이 제거된 가져온 새로운 지수 정보들 DB에 저장
            indexInfoRepository.saveAll(newIndexInfoList);

            pageNo++;
            //if(itemList.isEmpty())  break;
            if(pageNo == 5) break;
        }
    }


    @Override
    public MarketIndexApiResponse findAll() {
        MarketIndexApiResponse response = getOpenApiData(1, 100);
        return response;
    }

    public MarketIndexApiResponse getOpenApiData(int pageNo, int numOfRows) {
        return financeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getStockMarketIndex")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("resultType", "json")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(MarketIndexApiResponse.class)
                .block();
    }

}
