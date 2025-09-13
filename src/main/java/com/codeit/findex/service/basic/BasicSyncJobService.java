package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.data.SyncJobDto;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.request.SyncJobSearchRequest;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.entity.*;
import com.codeit.findex.mapper.SyncJobMapper;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.repository.SyncJobRepository;
import com.codeit.findex.service.SyncJobService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicSyncJobService implements SyncJobService {

    @Value("${external.finance.service-key}")
    private String serviceKey;

    private final WebClient financeWebClient;
    private final IndexInfoRepository indexInfoRepository;
    private final SyncJobRepository syncJobRepository;
    private final SyncJobMapper syncJobMapper;
    private final IndexDataRepository indexDataRepository;

    @Transactional
    @Override
    public List<SyncJobDto> createIndexInfoSyncJob(String workerId) {
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
                            .result(ResultType.SUCCESS.toBoolean())
                            .build();
                }).toList();

        List<SyncJob> created = syncJobRepository.saveAll(syncJobList);

        return created.stream().map(syncJobMapper::toDto).toList();
    }

    @Transactional
    public List<SyncJobDto> createIndexDataSyncJob(String workerId, IndexDataSyncRequest request) {
        // 1. 지수 데이터 DB에 저장
        createIndexData(request);

        // 2. 지수 정보(지수분류명)에 해당하는 지수 데이터 조회
        List<SyncJob> syncJobList = indexDataRepository.findByIndexInfoIds(request.indexInfoIds()).stream()
                .map(indexData -> {
                    return  SyncJob.builder()
                            .indexInfo(indexData.getIndexInfo())
                            .jobType(JobType.INDEX_DATA)
                            .jobTime(Instant.now())
                            .targetDate(LocalDate.now())
                            .worker(workerId)
                            .result(ResultType.SUCCESS.toBoolean())
                            .build();
                }).toList();

        return syncJobRepository.saveAll(syncJobList).stream()
                .map(syncJobMapper::toDto).toList();
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

    /** OpenApi에서 받아온 데이터를 Index_Data DB에 저장 */
    public void createIndexData(IndexDataSyncRequest request) {

        int pageNo = 1;
        int pageSize = 999;

        // 1. request에서 준 날짜 형식 변환(검색용)
        String beginDate = request.baseDateFrom().replace("-", "");
        String endDate = request.baseDateTo().replace("-", "");

        // 2. DB에서 아이디에 해당하는 지수정보 조회
        List<IndexInfo> indexInfoList = indexInfoRepository.findAllById(request.indexInfoIds());

        // 3. 지수 정보가 올바르게 가져와졌는지 검증
        if (indexInfoList.size() != request.indexInfoIds().size()) {
            throw new IllegalArgumentException("존재하지 않는 지수정보가 포함되어 있습니다.");
        }

        Set<String> indexInfoSet = indexInfoList.stream().map(indexInfo -> indexInfo.getIndexClassification()+indexInfo.getIndexName()).collect(Collectors.toSet());

        // 5. OpenApi에서 가져온 baseDate를 LocalDate로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // OpenApi 호출
        while (true) {
            List<IndexData> indexDataList = getFromOpenApiByBaseDate(pageNo, pageSize, beginDate, endDate).getResponse().getBody().getItems().getItem().stream()
                    // 지수 분류명으로 필터링
                    .filter(item -> indexInfoSet.contains(item.getIndexClassification()+item.getIndexName()))
                    .map(item -> {
                        IndexInfo matchedInfo = indexInfoList.stream()
                                .filter(info -> info.getIndexName().equals(item.getIndexName()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("IndexInfo not found for " + item.getIndexName()));

                        return IndexData.builder()
                                .indexInfo(matchedInfo)   // 여기 넣기
                                .baseDate(LocalDate.parse(item.getBaseDate(), formatter))
                                .sourceType(SourceType.OPEN_API)
                                .marketPrice(item.getMarketPrice())
                                .closingPrice(item.getClosingPrice())
                                .highPrice(item.getHighPrice())
                                .lowPrice(item.getLowPrice())
                                .versus(item.getVersus())
                                .fluctuationRate(item.getFluctuationRate())
                                .tradingPrice(item.getTradingPrice())
                                .tradingQuantity(item.getTradingQuantity())
                                .marketTotalAmount(item.getMarketTotalAmount())
                                .build();
                    })
                    .toList();

            // 데이터 저장
            indexDataRepository.saveAll(indexDataList);

            pageNo++;
            if(indexDataList.isEmpty())  break;
        }
    }

    /** OpenApi에서 받아온 데이터로 Index_infos 값에 매핑 후 DB에 저장 */
    public void createIndexInfos() {
        int pageNo = 1;
        int pageSize = 500;

        Set<String> seen = new HashSet<>();

        // 5. OpenApi에서 가져온 baseDate를 LocalDate로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 1. OpenAPI 호출
        while (true) {
            // 데이터 1000개
            List<IndexInfo> newIndexInfoList = getFromOpenApiByPage(pageNo, pageSize).getResponse().getBody().getItems().getItem().stream()
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
                            .basePointInTime (LocalDate.parse(item.getBasePointInTime(), formatter))
                            .baseIndex(Double.valueOf(item.getBaseIndex()))
                            .sourceType(SourceType.OPEN_API)
                            .favorite(false)
                            .build()
                    )
                    .toList();

            // 중복이 제거된 가져온 새로운 지수 정보들 DB에 저장
            try {
                indexInfoRepository.saveAll(newIndexInfoList);
            } catch (DataIntegrityViolationException e) {
                System.out.println(e.getMessage());
            }


            pageNo++;
            if(newIndexInfoList.isEmpty())  break;
        }
    }

    public MarketIndexApiResponse getFromOpenApiByPage(int pageNo, int numOfRows) {
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

    public MarketIndexApiResponse getFromOpenApiByBaseDate(int pageNo, int numOfRows, String beginDate, String endDate) {

        if(beginDate == null || beginDate.length() != 8) throw new IllegalArgumentException("잘못된 날짜 정보입니다.");
        if(endDate == null || endDate.length() != 8) throw new IllegalArgumentException("잘못된 날짜 정보입니다.");

        return financeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getStockMarketIndex")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("resultType", "json")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("beginBasDt", beginDate)
                        .queryParam("endBasDt", endDate)
                        .build())
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(MarketIndexApiResponse.class)
                .block();
    }
}