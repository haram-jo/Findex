package com.codeit.findex.repository.custom;

import com.codeit.findex.entity.AutoSync;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository // 스프링 컴포넌트 스캔 대상으로 지정
public class AutoSyncRepositoryImpl implements AutoSyncRepositoryCustom { // 커스텀 리포지토리 구현체

    @PersistenceContext
    private EntityManager em; // JPA 엔티티 매니저 주입

    @Override
    public Slice<AutoSync> findSlice( // 커서 기반 keyset 페이지네이션 + Slice 조립
                                      Long indexInfoId,         // 지수 필터
                                      Boolean enabled,          // 활성화 필터
                                      String sortField,         // 정렬 필드 (화이트리스트 전제)
                                      boolean asc,              // 오름차순 여부
                                      Object cursorValue,       // 마지막 정렬값 (문자열 or 불리언)
                                      Long idAfter,             // 마지막 ID
                                      int size                  // 페이지 크기
    ) {
        // ===== 1) 기본 SELECT (N+1 방지 위해 indexInfo 조인 페치) =====
        StringBuilder jpql = new StringBuilder(); // JPQL 문자열 빌더
        jpql.append("select a ")                 // AutoSync 엔티티만 선택 (Slice에서 엔티티 리스트 사용)
                .append("from AutoSync a ")         // AutoSync 별칭 a
                .append("join fetch a.indexInfoId i "); // indexInfo 즉시 로딩으로 N+1 회피

        // ===== 2) 동적 WHERE 절 구성 =====
        List<String> where = new ArrayList<>(); // 가변 where 절 목록 준비
        if (indexInfoId != null) {              // 지수 필터가 존재하면
            where.add("i.id = :indexInfoId");   // i.id 조건 추가
        }
        if (enabled != null) {                  // 활성화 필터가 존재하면
            where.add("a.enabled = :enabled");  // a.enabled 조건 추가
        }

        // 커서가 있는 경우에만 keyset 경계 조건을 추가한다
        if (cursorValue != null || idAfter != null) { // 최소 하나라도 있으면 경계 조건 구성
            if ("enabled".equals(sortField)) {        // 불리언 정렬 케이스
                if (asc) {                            // 오름차순일 때
                    where.add("( (a.enabled > :cval) or (a.enabled = :cval and a.id > :cid) )"); // keyset 이후
                } else {
                    where.add("( (a.enabled < :cval) or (a.enabled = :cval and a.id < :cid) )"); // 내림차순용
                }
            } else {                                   // "indexInfo.indexName" 문자열 정렬 케이스
                if (asc) {
                    where.add("( (i.indexName > :cval) or (i.indexName = :cval and a.id > :cid) )"); // 사전순 asc
                } else {
                    where.add("( (i.indexName < :cval) or (i.indexName = :cval and a.id < :cid) )"); // 사전순 desc
                }
            }
        }

        // where 절이 하나 이상이면 "where" 키워드로 연결한다
        if (!where.isEmpty()) {                        // 조건이 존재하면
            jpql.append("where ")                      // where 시작
                    .append(String.join(" and ", where))   // and 로 연결
                    .append(" ");                          // 한 칸 공백
        }

        // ===== 3) ORDER BY (정렬 방향 + tie-breaker id) =====
        if ("enabled".equals(sortField)) {             // 불리언 정렬이면
            jpql.append("order by a.enabled ");        // enabled 우선 정렬
        } else {
            jpql.append("order by i.indexName ");      // indexName 우선 정렬
        }
        jpql.append(asc ? "asc " : "desc ");           // 방향 지정
        jpql.append(", a.id ").append(asc ? "asc " : "desc "); // 동점자 안정화용 id 정렬

        // ===== 4) 쿼리 생성 및 파라미터 바인딩 =====
        TypedQuery<AutoSync> query = em.createQuery(jpql.toString(), AutoSync.class); // JPQL → TypedQuery

        if (indexInfoId != null) {                     // 지수 필터 바인딩
            query.setParameter("indexInfoId", indexInfoId); // 파라미터 설정
        }
        if (enabled != null) {                         // 활성화 필터 바인딩
            query.setParameter("enabled", enabled);    // 파라미터 설정
        }
        if (cursorValue != null || idAfter != null) {  // 커서 경계 파라미터 바인딩
            Object cval = cursorValue;                 // cval = 정렬 필드의 마지막 값
            if (cval == null) {                        // 값이 null이면
                // 정렬 타입별로 가장 작은(또는 큰) 값을 대신 넣어 비교의 기준을 잡는다
                cval = "enabled".equals(sortField) ? Boolean.FALSE : ""; // asc 기준 최소값
            }
            long cid = (idAfter == null ? (asc ? Long.MIN_VALUE : Long.MAX_VALUE) : idAfter); // id 경계값
            query.setParameter("cval", cval);          // 정렬값 파라미터
            query.setParameter("cid", cid);            // id 파라미터
        }

        // ===== 5) size+1로 조회하여 hasNext 판단 =====
        query.setMaxResults(size + 1);                 // 다음 페이지 유무 확인용으로 1건 더 가져온다
        List<AutoSync> rows = query.getResultList();   // 결과 리스트 실행

        boolean hasNext = rows.size() > size;          // 초과분 존재 = 다음 페이지 있음
        if (hasNext) {                                 // 초과분이 있다면
            rows = rows.subList(0, size);              // 보여줄 사이즈만큼 자른다
        }

        // ===== 6) Slice로 감싸서 반환 =====
        return new SliceImpl<>(rows,                    // 현 페이지 데이터
                org.springframework.data.domain.PageRequest.of(0, size), // offset 개념은 사용 안 함(의미상 placeholder)
                hasNext);                               // 다음 페이지 존재 여부
    }

    @Override
    public long countByFilters(Long indexInfoId, Boolean enabled) { // 동일 필터 기준 총 개수
        StringBuilder jpql = new StringBuilder();       // JPQL 빌더
        jpql.append("select count(a) ")                 // 카운트 선택
                .append("from AutoSync a join a.indexInfoId i "); // 조인(페치 불필요)

        List<String> where = new ArrayList<>();         // where 절 모음
        if (indexInfoId != null) {                      // 지수 필터
            where.add("i.id = :indexInfoId");           // 조건 추가
        }
        if (enabled != null) {                          // 활성화 필터
            where.add("a.enabled = :enabled");          // 조건 추가
        }
        if (!where.isEmpty()) {                         // where 구성
            jpql.append("where ").append(String.join(" and ", where)).append(" "); // and 연결
        }

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class); // TypedQuery 생성
        if (indexInfoId != null) {                      // 파라미터 바인딩
            q.setParameter("indexInfoId", indexInfoId); // 지수 id
        }
        if (enabled != null) {                          // 파라미터 바인딩
            q.setParameter("enabled", enabled);         // enabled
        }
        return q.getSingleResult();                     // 총 개수 반환
    }
}
