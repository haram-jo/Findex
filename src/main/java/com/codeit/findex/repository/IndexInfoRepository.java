package com.codeit.findex.repository;

import com.codeit.findex.dto.data.IndexInfoSummaryDto;
import com.codeit.findex.repository.custom.IndexInfoRepositoryCustom;
import com.codeit.findex.entity.IndexInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/*서비스 내 비즈니스 로직이 길어지고 복잡해져서,
페이징, 쿼리 처리를 따로 관리하기 위해
CustomRepository로 분리
 */
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoRepositoryCustom {

    boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);

    //지수 요약 목록 조회
    @Query("SELECT new com.codeit.findex.dto.data.IndexInfoSummaryDto(i.id, i.indexClassification, i.indexName) FROM IndexInfo i")
    List<IndexInfoSummaryDto> findAllSummaries();
}