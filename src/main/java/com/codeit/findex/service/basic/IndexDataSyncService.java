package com.codeit.findex.service.basic;

import com.codeit.findex.client.MarketIndexApiClient;
import com.codeit.findex.dto.request.IndexDataSyncRequest;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IndexDataSyncService {

    private final MarketIndexApiClient marketIndexApiClient;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

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

        // 5. OpenApi에서 가져온 baseDate를 LocalDate로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (IndexInfo indexInfo: indexInfoList) {
            List<IndexData> targetIndexData = new ArrayList<>();


            while (true) {
                List<MarketIndexApiResponse.Item> fetchedIndexData =
                        marketIndexApiClient.getFromOpenApiByBaseDate(pageNo, pageSize, indexInfo.getIndexName(), beginDate, endDate);

                if(fetchedIndexData.isEmpty())  break; // 비어있으면 루프 중단

                targetIndexData.addAll(fetchedIndexData.stream()
                        .filter(item ->
                                Objects.equals(item.getIndexName(), indexInfo.getIndexName()) &&
                                Objects.equals(item.getIndexClassification(), indexInfo.getIndexClassification())
                        ).map(item -> {
                            IndexInfo matchedInfo = indexInfoList.stream()
                                    .filter(info -> info.getIndexName().equals(item.getIndexName()))
                                    .findFirst()
                                    .orElseThrow(() -> new IllegalArgumentException("IndexInfo not found for " + item.getIndexName()));

                            return IndexData.builder()
                                    .indexInfo(matchedInfo)
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
                        }).toList());
                pageNo++;
            }

            if (!targetIndexData.isEmpty()) {
                indexDataRepository.saveAllInBatch(targetIndexData, indexInfoList.get(0).getId());
            }
        }
    }
}
