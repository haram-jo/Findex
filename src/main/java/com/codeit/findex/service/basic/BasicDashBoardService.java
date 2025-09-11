package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.dto.response.MajorIndexDataResponse;
import com.codeit.findex.repository.DashBoardRepository;
import com.codeit.findex.service.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicDashBoardService implements DashBoardService {

    private final DashBoardRepository dashBoardRepository;

    @Override
    public List<MajorIndexDataResponse> getMajorIndex(String periodType) {

        List<MajorIndexDto> rawData = dashBoardRepository.getFavoriteMajorIndexData(periodType);

        Map<Long, List<MajorIndexDto>> grouped = rawData.stream()
                .collect(Collectors.groupingBy(MajorIndexDto::indexInfoId, Collectors.toList()));

        return grouped.values().stream()
                .map(list -> {
                    list.sort(Comparator.comparing(MajorIndexDto::baseDate).reversed());

                    MajorIndexDto current = list.get(0);
                    MajorIndexDto before = list.size() > 1 ? list.get(1) : null;

                    BigDecimal currentPrice = current.closingPrice();
                    BigDecimal beforePrice = before != null ? before.closingPrice() : currentPrice;

                    BigDecimal versus = currentPrice.subtract(beforePrice);
                    BigDecimal fluctuationRate = beforePrice.compareTo(BigDecimal.ZERO) != 0
                            ? versus.divide(beforePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;

                    return new MajorIndexDataResponse(
                            current.indexInfoId(),
                            current.indexClassification(),
                            current.indexName(),
                            versus,
                            fluctuationRate,
                            currentPrice,
                            beforePrice
                    );
                })
                .toList();
    }
}
