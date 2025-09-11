package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import com.codeit.findex.custom.IndexDataRepositoryCustom;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>, IndexDataRepositoryCustom {

    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

    /**
     * indexInfo와 indexData를 가져오는 한방쿼리
     */
    @Query("""
        SELECT d
        FROM IndexData d
            JOIN FETCH d.indexInfo i
            WHERE i.id IN :indexInfoIds
    """)
    List<IndexData> findByIndexInfoIds(List<Long> indexInfoIds);
}