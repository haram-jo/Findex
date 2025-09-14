package com.codeit.findex.service.basic;

import com.codeit.findex.client.MarketIndexApiClient;
import com.codeit.findex.dto.data.IndexInfoUnique;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.entity.*;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.repository.SyncJobRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OpenAPI에서 가져온 데이터를 가공해서 IndexInfo 테이블에 저장
 */
@Service
@RequiredArgsConstructor
public class IndexInfoSyncService {

    private final IndexInfoRepository indexInfoRepository;
    private final SyncJobRepository syncJobRepository;
    private final MarketIndexApiClient marketIndexApiClient;

    /** OpenApi에서 받아온 데이터로 Index_infos 값에 매핑 후 DB에 저장 */
    public void createIndexInfos() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // yyyMMdd 를 LocalDate(yyyy-MM-dd) 형식으로 변환해주는 포매터
        SyncJob lastSyncJob = syncJobRepository.findTopByJobTypeOrderByJobTimeDesc(JobType.INDEX_INFO).orElse(null); // DB에서 최신 SyncJob 조회

        // 금융위원회 OpenAPI에 보내는 검색조건
        int pageNo = 1; // 페이지 번호
        int pageSize = 999; // 한 페이지 결과 수
        String lastSyncedDate = lastSyncJob != null ? lastSyncJob.getJobTime().format(formatter) : null; // 기준일자가 검색값보다 크거나 같은 데이터를 검색

        List<IndexInfo> indexInfoRegistry = new ArrayList<>(); // IndexInfo 테이블에 최종적으로 저장되는 데이터 목록

        // 이미 디비에 존재하는 index infos를 조회해서 IndexInfoUnique로 변환 ex) IndexInfoUnique[indexClassification=테마지수, indexName=KRX/S&P 탄소효율 그린뉴딜지수]
        Set<IndexInfoUnique> existIndexInfos = indexInfoRepository.findAll().stream()
                .map(item -> IndexInfoUnique.builder()
                        .indexClassification(item.getIndexClassification())
                        .indexName(item.getIndexName())
                        .build())
                .collect(Collectors.toSet());

        while (true) {
            // 1. OpenAPI에서 가져온 순수 응답데이터
            List<MarketIndexApiResponse.Item> fetchedIndexInfos = marketIndexApiClient.getFromOpenApiByPage(pageNo, pageSize, lastSyncedDate).getResponse().getBody().getItems().getItem();

            System.out.println(pageNo + ", " + pageSize);

            if(fetchedIndexInfos.isEmpty())  break; // 비어있으면 루프 중단

            for (MarketIndexApiResponse.Item item : fetchedIndexInfos) {
                IndexInfoUnique uniqueKey = IndexInfoUnique.builder()
                        .indexClassification(item.getIndexClassification())
                        .indexName(item.getIndexName())
                        .build();

                if (existIndexInfos.contains(uniqueKey)) {
                    continue;
                }

                IndexInfo newIndexInfo = IndexInfo.builder()
                        .indexClassification(item.getIndexClassification()) // 지수 분류 명
                        .indexName(item.getIndexName()) // 지수명
                        .employedItemsCount(Integer.valueOf(item.getEmployedItemsCount())) //채용 종목 수
                        .basePointInTime (LocalDate.parse(item.getBasePointInTime(), formatter))
                        .baseIndex(Double.valueOf(item.getBaseIndex()))
                        .sourceType(SourceType.OPEN_API)
                        .favorite(false)
                        .build();

                indexInfoRegistry.add(newIndexInfo);
                existIndexInfos.add(uniqueKey);
            }

            pageNo++;
        }

        if (!indexInfoRegistry.isEmpty()) {
            indexInfoRepository.saveAllInBatch(indexInfoRegistry);
        }
    }
}
