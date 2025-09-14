package com.codeit.findex.client;

import com.codeit.findex.dto.response.MarketIndexApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketIndexApiClient {

    @Value("${external.finance.service-key}")
    private String serviceKey;

    private final WebClient webClient;

    /**
     * OpenApi에서 날짜를 기준으로 데이터를 받아옴
     * @param pageNo 페이지 번호
     * @param numOfRows 가져오는 row 개수
     * @param lastSyncedDate 기준일자가 검색값보다 크거나 같은 데이터를 검색
     */
    public MarketIndexApiResponse getFromOpenApiByPage(int pageNo, int numOfRows, String lastSyncedDate) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/getStockMarketIndex")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("resultType", "json")
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", numOfRows);

                    if (lastSyncedDate != null && !lastSyncedDate.isBlank()) {
                        uriBuilder.queryParam("beginBasDt", lastSyncedDate); // 기준일자 조건 추가
                    }

                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(MarketIndexApiResponse.class)
                .block();
    }


    public List<MarketIndexApiResponse.Item> getFromOpenApiByBaseDate(int pageNo, int numOfRows, String indexName, String beginDate, String endDate) {
        if(beginDate == null || beginDate.length() != 8) throw new IllegalArgumentException("잘못된 날짜 정보입니다.");
        if(endDate == null || endDate.length() != 8) throw new IllegalArgumentException("잘못된 날짜 정보입니다.");

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getStockMarketIndex")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("resultType", "json")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("idxNm", indexName)
                        .queryParam("beginBasDt", beginDate)
                        .queryParam("endBasDt", endDate)
                        .build())
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(MarketIndexApiResponse.class)
                .block().getResponse().getBody().getItems().getItem();
    }

}
