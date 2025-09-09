package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>{

    // 데이터 등록 시 중복 체크
    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);
}