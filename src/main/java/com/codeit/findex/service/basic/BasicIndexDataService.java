package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.CursorPageResponseIndexDataDto;
import com.codeit.findex.dto.data.IndexDataDto;
import com.codeit.findex.dto.request.IndexDataCreateRequest;
import com.codeit.findex.dto.request.IndexDataSearchCondition;
import com.codeit.findex.dto.request.IndexDataUpdateRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.mapper.IndexDataMapper;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexDataService;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.io.Writer;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicIndexDataService implements IndexDataService {

    private final IndexDataRepository indexDataRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataMapper indexDataMapper;

    @Override
    @Transactional
    public IndexDataDto createIndexData(IndexDataCreateRequest request) {
        if (indexDataRepository.existsByIndexInfoIdAndBaseDate(request.indexInfoId(), request.baseDate())) {
            throw new IllegalArgumentException("이미 해당 날짜에 등록된 지수 데이터가 존재합니다.");
        }

        IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 지수 정보를 찾을 수 없습니다: " + request.indexInfoId()));

        IndexData indexData = indexDataMapper.toEntity(request, indexInfo, SourceType.USER);

        IndexData savedIndexData = indexDataRepository.save(indexData);

        return indexDataMapper.toDto(savedIndexData);
    }

    @Override
    @Transactional
    public IndexDataDto updateIndexData(Long id, IndexDataUpdateRequest request) {
        IndexData indexData = indexDataRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 주가 데이터를 찾을 수 없습니다: " + id));

        if (!isUpdateNeeded(request, indexData)) {
            return indexDataMapper.toDto(indexData);
        }

        indexData.update(
                Objects.requireNonNullElse(request.marketPrice(), indexData.getMarketPrice()),
                Objects.requireNonNullElse(request.closingPrice(), indexData.getClosingPrice()),
                Objects.requireNonNullElse(request.highPrice(), indexData.getHighPrice()),
                Objects.requireNonNullElse(request.lowPrice(), indexData.getLowPrice()),
                Objects.requireNonNullElse(request.versus(), indexData.getVersus()),
                Objects.requireNonNullElse(request.fluctuationRate(), indexData.getFluctuationRate()),
                Objects.requireNonNullElse(request.tradingQuantity(), indexData.getTradingQuantity()),
                Objects.requireNonNullElse(request.tradingPrice(), indexData.getTradingPrice()),
                Objects.requireNonNullElse(request.marketTotalAmount(), indexData.getMarketTotalAmount())
        );

        return indexDataMapper.toDto(indexData);
    }

    @Override
    @Transactional
    public void deleteIndexData(Long id) {
        if (!indexDataRepository.existsById(id)) {
            throw new EntityNotFoundException("해당 ID의 주가 데이터를 찾을 수 없습니다: " + id);
        }
        indexDataRepository.deleteById(id);
    }

    @Override
    public CursorPageResponseIndexDataDto searchIndexData(IndexDataSearchCondition condition) {
        List<IndexData> results = indexDataRepository.search(condition);
        long totalElements = indexDataRepository.count(condition);

        boolean hasNext = results.size() > condition.size();
        if (hasNext) {
            results.remove(results.size() - 1);
        }

        List<IndexDataDto> content = indexDataMapper.toDtoList(results);

        String nextCursor = null;
        Long nextIdAfter = null;

        if (hasNext) {
            IndexData lastItem = results.get(results.size() - 1);
            String cursorJson = String.format("{\"id\":%d}", lastItem.getId());
            nextCursor = Base64.getEncoder().encodeToString(cursorJson.getBytes());
            nextIdAfter = lastItem.getId();
        }

        return new CursorPageResponseIndexDataDto(
                content,
                nextCursor,
                nextIdAfter,
                condition.size(),
                totalElements,
                hasNext
        );
    }

    private boolean isUpdateNeeded(IndexDataUpdateRequest request, IndexData indexData) {
        if (request.marketPrice() != null && indexData.getMarketPrice().compareTo(request.marketPrice()) != 0) return true;
        if (request.closingPrice() != null && indexData.getClosingPrice().compareTo(request.closingPrice()) != 0) return true;
        if (request.highPrice() != null && indexData.getHighPrice().compareTo(request.highPrice()) != 0) return true;
        if (request.lowPrice() != null && indexData.getLowPrice().compareTo(request.lowPrice()) != 0) return true;
        if (request.versus() != null && indexData.getVersus().compareTo(request.versus()) != 0) return true;
        if (request.fluctuationRate() != null && indexData.getFluctuationRate().compareTo(request.fluctuationRate()) != 0) return true;
        if (request.tradingQuantity() != null && !indexData.getTradingQuantity().equals(request.tradingQuantity())) return true;
        if (request.tradingPrice() != null && !indexData.getTradingPrice().equals(request.tradingPrice())) return true;
        return request.marketTotalAmount() != null && !indexData.getMarketTotalAmount().equals(request.marketTotalAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public void exportIndexDataToCsv(Writer writer, IndexDataSearchCondition condition) {
        List<IndexData> indexDataList = indexDataRepository.findAllByCondition(condition);
        List<IndexDataDto> indexDataDtoList = indexDataMapper.toDtoList(indexDataList);

        String[] headers = {"baseDate", "marketPrice", "closingPrice", "highPrice", "lowPrice", "versus", "fluctuationRate", "tradingQuantity", "tradingPrice", "marketTotalAmount"};

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {
            for (IndexDataDto dto : indexDataDtoList) {
                csvPrinter.printRecord(
                        dto.baseDate(),
                        dto.marketPrice(),
                        dto.closingPrice(),
                        dto.highPrice(),
                        dto.lowPrice(),
                        dto.versus(),
                        dto.fluctuationRate(),
                        dto.tradingQuantity(),
                        dto.tradingPrice(),
                        dto.marketTotalAmount()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV export failed", e);
        }
    }
}
