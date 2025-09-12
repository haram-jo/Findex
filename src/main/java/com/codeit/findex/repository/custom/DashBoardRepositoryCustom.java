package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.MajorIndexDto;
import com.codeit.findex.dto.response.MajorIndexDataResponse;

import java.util.List;

public interface DashBoardRepositoryCustom {
    List<MajorIndexDto> getFavoriteMajorIndexData(String periodType);
}
