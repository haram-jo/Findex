package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.IndexDataDto;
import com.codeit.findex.dto.request.IndexDataCreateRequest;
import com.codeit.findex.dto.request.IndexDataUpdateRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.mapper.IndexDataMapper;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexDataService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
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
}
