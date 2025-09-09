package com.codeit.findex.dto.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndexDataUpdateRequest {

    private BigDecimal marketPrice;
    private BigDecimal closingPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal versus;
    private BigDecimal fluctuationRate;
    private Long tradingQuantity;
    private Long tradingPrice;
    private Long marketTotalAmount;
}