package com.codeit.findex.repository.custom;

import com.codeit.findex.dto.data.MajorIndexDto;

import java.util.List;

public interface DashBoardRepositoryCustom {
    List<MajorIndexDto> getFavoriteMajorIndexData(int month);
}
