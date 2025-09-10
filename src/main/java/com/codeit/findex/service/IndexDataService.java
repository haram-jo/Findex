package com.codeit.findex.service;

import com.codeit.findex.dto.data.CursorPageResponseIndexDataDto;
import com.codeit.findex.dto.data.IndexDataDto;
import com.codeit.findex.dto.request.IndexDataCreateRequest;
import com.codeit.findex.dto.request.IndexDataSearchCondition;
import com.codeit.findex.dto.request.IndexDataUpdateRequest;

public interface IndexDataService {
    IndexDataDto createIndexData(IndexDataCreateRequest request);
    void deleteIndexData(Long id);
    IndexDataDto updateIndexData(Long id, IndexDataUpdateRequest request);
    CursorPageResponseIndexDataDto searchIndexData(IndexDataSearchCondition condition);
}