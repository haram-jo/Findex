package com.codeit.findex.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "index_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "source_type", length = 100)
    private String sourceType;

    @Column(name = "market_price", precision = 20, scale = 4)
    private BigDecimal marketPrice; // 시가

    @Column(name = "closing_price", precision = 20, scale = 4)
    private BigDecimal closingPrice; // 종가

    @Column(name = "high_price", precision = 20, scale = 4)
    private BigDecimal highPrice; // 고가

    @Column(name = "low_price", precision = 20, scale = 4)
    private BigDecimal lowPrice; // 저가

    @Column(name = "versus", precision = 20, scale = 4)
    private BigDecimal versus; // 대비

    @Column(name = "fluctuation_rate", precision = 10, scale = 4)
    private BigDecimal fluctuationRate; // 등락률

    @Column(name = "trading_quantity")
    private Long tradingQuantity; // 거래량

    @Column(name = "trading_price")
    private Long tradingPrice; // 거래대금

    @Column(name = "market_total_amount")
    private Long marketTotalAmount; // 상장 시가 총액


    @Builder
    public IndexData(IndexInfo indexInfo, LocalDate baseDate, String sourceType, BigDecimal marketPrice,
                     BigDecimal closingPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal versus,
                     BigDecimal fluctuationRate, Long tradingQuantity, Long tradingPrice, Long marketTotalAmount) {
        this.indexInfo = indexInfo;
        this.baseDate = baseDate;
        this.sourceType = sourceType;
        this.marketPrice = marketPrice;
        this.closingPrice = closingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.versus = versus;
        this.fluctuationRate = fluctuationRate;
        this.tradingQuantity = tradingQuantity;
        this.tradingPrice = tradingPrice;
        this.marketTotalAmount = marketTotalAmount;
    }

    public void update(BigDecimal marketPrice, BigDecimal closingPrice, BigDecimal highPrice, BigDecimal lowPrice,
                       BigDecimal versus, BigDecimal fluctuationRate, Long tradingQuantity, Long tradingPrice,
                       Long marketTotalAmount) {
        this.marketPrice = marketPrice;
        this.closingPrice = closingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.versus = versus;
        this.fluctuationRate = fluctuationRate;
        this.tradingQuantity = tradingQuantity;
        this.tradingPrice = tradingPrice;
        this.marketTotalAmount = marketTotalAmount;
    }
}