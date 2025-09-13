package com.codeit.findex.dto.data;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ChartDataRow(
    LocalDate date,
    BigDecimal value,
    BigDecimal ma5,
    BigDecimal ma20
){}
