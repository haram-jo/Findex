package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.CursorPageResponseSyncJobDto;
import com.codeit.findex.dto.response.MarketIndexApiResponse;
import com.codeit.findex.service.SyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class BasicSyncJobService implements SyncJobService {

    @Value("${external.finance.service-key}")
    private String serviceKey;

    private final WebClient financeWebClient;

    @Override
    public MarketIndexApiResponse findAll() {
        MarketIndexApiResponse response = financeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getStockMarketIndex")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("resultType", "json")
                        .queryParam("pageNo", 1) // 페이지 번호
                        .queryParam("numOfRows", 10) // 한 페이지 결과 수
                        .build())
                .accept(MediaType.ALL)  // Content-Type 무시하고 수락
                .retrieve()
                .bodyToMono(MarketIndexApiResponse.class)
                .block();

        return response;
    }

}
