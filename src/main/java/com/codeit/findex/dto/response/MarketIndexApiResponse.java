package com.codeit.findex.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketIndexApiResponse {

    @JsonProperty("response")
    private ResponseWrapper response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseWrapper {
        @JsonProperty("body")
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;

        @JsonProperty("items")
        private Items items;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        // 지수명
        @JsonProperty("idxNm")
        private String indexName; // OpenAPI 실제 응답 필드명

        // 지수 분류명
        @JsonProperty("idxCsf")
        private String indexClassification;

        // 기준 지수
        @JsonProperty("basIdx")
        private String baseIndex;

        // 기준 시점
        @JsonProperty("basPntm")
        private String basePointInTime;

        // 기준 날짜
        @JsonProperty("basDt")
        private String baseDate;

        // 시가
        @JsonProperty("mkp")
        private BigDecimal marketPrice;

        // 종가
        @JsonProperty("clpr")
        private BigDecimal closingPrice;

        // 고가
        @JsonProperty("hipr")
        private BigDecimal highPrice;

        // 저가
        @JsonProperty("lopr")
        private BigDecimal lowPrice;

        // 대비
        @JsonProperty("vs")
        private BigDecimal versus;

        // 등락률
        @JsonProperty("fltRt")
        private BigDecimal fluctuationRate;

        // 거래량
        @JsonProperty("trqu")
        private Long tradingQuantity;

        // 거래대금
        @JsonProperty("trPrc")
        private Long tradingPrice;

        // 상장 시가 총액
        @JsonProperty("lstgMrktTotAmt")
        private Long marketTotalAmount;

        // 채용 종목 수
        @JsonProperty("epyItmsCnt")
        private String employedItemsCount;
    }



}
