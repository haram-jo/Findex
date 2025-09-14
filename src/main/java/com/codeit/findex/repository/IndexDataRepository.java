package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import com.codeit.findex.repository.custom.IndexDataRepositoryCustom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query("""
        SELECT d
        FROM IndexData d
        JOIN FETCH d.indexInfo i
        WHERE i.id = :indexInfoId
          AND d.baseDate = (
              SELECT MAX(d2.baseDate)
              FROM IndexData d2
              WHERE d2.indexInfo.id = i.id
          )
    """)
    Optional<IndexData> findLatestByIndexInfoId(Long indexInfoId);

    @Query("select d.baseDate from IndexData d where d.indexInfo.id = :indexInfoId and d.baseDate in :dates")
    List<LocalDate> findExistingDates(Long indexInfoId, List<LocalDate> dates);

    @Query("select distinct d.indexInfo.id from IndexData d")
    List<Long> findDistinctIndexInfoIds();
}