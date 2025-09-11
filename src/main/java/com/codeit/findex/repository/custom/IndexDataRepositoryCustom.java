package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.request.IndexDataSearchCondition;
import com.codeit.findex.entity.IndexData;

import java.util.List;

public interface IndexDataRepositoryCustom {

    List<IndexData> search(IndexDataSearchCondition condition);

    long count(IndexDataSearchCondition condition);

    List<IndexData> findAllByCondition(IndexDataSearchCondition condition);

}
