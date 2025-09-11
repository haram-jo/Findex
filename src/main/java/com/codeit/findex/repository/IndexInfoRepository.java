package com.codeit.findex.repository;

import com.codeit.findex.repository.custom.IndexInfoRepositoryCustom;
import com.codeit.findex.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/*서비스 내 비즈니스 로직이 길어지고 복잡해져서,
페이징, 쿼리 처리를 따로 관리하기 위해
CustomRepository로 분리
 */
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoRepositoryCustom {
    boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);
}