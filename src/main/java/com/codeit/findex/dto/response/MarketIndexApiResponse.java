package com.codeit.findex.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
        private String marketPrice;

        // 종가
        @JsonProperty("clpr")
        private String closingPrice;

        // 고가
        @JsonProperty("hipr")
        private String highPrice;

        // 저가
        @JsonProperty("lopr")
        private String lowPrice;

        // 대비
        @JsonProperty("vs")
        private String versus;

        // 등락률
        @JsonProperty("fltRt")
        private String fluctuationRate;

        // 거래량
        @JsonProperty("trqu")
        private String tradingQuantity;

        // 거래대금
        @JsonProperty("trPrc")
        private String tradingPrice;

        // 상장 시가 총액
        @JsonProperty("lstgMrktTotAmt")
        private String marketTotalAmount;
    }



}
