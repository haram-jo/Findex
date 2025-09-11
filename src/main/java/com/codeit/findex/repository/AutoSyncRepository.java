package com.codeit.findex.repository;

import com.codeit.findex.entity.AutoSync;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutoSyncRepository extends JpaRepository<AutoSync, Long> {

    // 전체 조회 시에도 indexInfo를 함께 로딩하여 N+1 최소화
    @Override
    @EntityGraph(attributePaths = "indexInfoId")
    List<AutoSync> findAll();

    // AutoSync.indexInfoId 는 IndexInfo 타입 필드이므로 .id 로 접근 // Schedular를 위한 추가
    @Query("select a.indexInfoId.id from AutoSync a where a.enabled = true")
    List<Long> findEnabledIndexIds();


//    // 지수 + 활성화
//    @EntityGraph(attributePaths = "indexInfoId")
//    List<AutoSync> findByIndexInfoId_IdAndEnabled(Long indexInfoId, boolean enabled);
//
//    // 지수만
//    @EntityGraph(attributePaths = "indexInfoId")
//    List<AutoSync> findByIndexInfoId_Id(Long indexInfoId);
//
//    // 활성화만
//    @EntityGraph(attributePaths = "indexInfoId")
//    List<AutoSync> findByEnabled(boolean enabled);
}
