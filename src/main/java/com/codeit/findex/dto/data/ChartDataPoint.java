package com.codeit.findex.dto.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChartDataPoint(
        LocalDate date,
        BigDecimal value
) {}