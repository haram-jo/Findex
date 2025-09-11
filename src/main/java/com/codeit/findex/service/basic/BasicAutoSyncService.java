package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.AutoSyncConfigDto;
import com.codeit.findex.dto.data.CursorPageResponseAutoSyncConfigDto;
import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.mapper.AutoSyncMapper;
import com.codeit.findex.repository.custom.AutoSyncRepositoryCustom;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.service.AutoSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor // final 필드 생성자 주입
@Service // 스프링 빈 등록
public class BasicAutoSyncService implements AutoSyncService { // 서비스 구현체

    private final AutoSyncRepository autoSyncRepository; // PATCH 에서 단건 조회/갱신 용도
    private final AutoSyncRepositoryCustom autoSyncRepositoryCustom; // Slice + keyset 전용 커스텀 리포지토리
    private final AutoSyncMapper autoSyncMapper;         // 엔티티↔DTO 변환기

    // ============PATCH=======================
    @Transactional
    @Override
    public AutoSyncConfigDto updateEnabled(Long id, Boolean enabled) { // PATCH: 활성화 토글
        if (enabled == null) {                                         // 유효성 검증
            throw new IllegalArgumentException("`enabled` must not be null."); // 오류 메시지
        }
        AutoSync entity = autoSyncRepository.findById(id)              // ID로 조회
                .orElseThrow(() -> new IllegalArgumentException("AutoSync not found: " + id)); // 없으면 예외
        entity.setEnabled(enabled);                                    // 값 변경 (더티체킹으로 flush)
        return autoSyncMapper.toDto(entity);                           // DTO 변환 후 반환
    }

    // ============목록조회=======================
    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseAutoSyncConfigDto list( // 목록 조회(리팩토링: Slice + keyset)
                                                     Long indexInfoId,                        // 지수 필터
                                                     Boolean enabled,                         // 활성화 필터
                                                     Long idAfter,                            // 마지막 ID
                                                     String cursor,                           // 마지막 정렬값(문자/불리언 문자열)
                                                     String sortField,                        // 정렬 필드
                                                     String sortDirection,                    // 정렬 방향
                                                     Integer size                             // 페이지 크기
    ) {
        int pageSize = normalizeSize(size);          // 페이지 크기 정규화
        String safeSortField = normalizeSortField(sortField); // 화이트리스트 강제
        boolean asc = !"desc".equalsIgnoreCase(sortDirection); // 정렬 방향 판정

        Object cursorValue = parseCursorValue(cursor, safeSortField); // 정렬값 타입에 맞게 파싱

        Slice<AutoSync> slice = autoSyncRepositoryCustom.findSlice(    // 커스텀 리포지토리 호출
                indexInfoId,                                          // 지수 필터
                enabled,                                              // 활성화 필터
                safeSortField,                                        // 안전한 정렬 필드
                asc,                                                  // 정렬 방향
                cursorValue,                                          // 정렬 커서 값
                idAfter,                                              // 마지막 ID
                pageSize                                              // 페이지 크기
        );

        List<AutoSync> rows = slice.getContent();                     // Slice 내용 추출
        boolean hasNext = slice.hasNext();                            // 다음 페이지 여부

        List<com.codeit.findex.dto.data.AutoSyncConfigDto> content = autoSyncMapper.toDtoList(rows); // DTO 리스트 변환

        String nextCursor = null;                                     // 다음 커서(정렬값)
        Long nextIdAfter = null;                                      // 다음 시작점 ID

        if (!rows.isEmpty()) {                                      // 결과가 있으면
            AutoSync last = rows.get(rows.size() - 1);              // 마지막 요소
            if (hasNext) {                                          // ★ 변경 지점: 다음 페이지가 있을 때만 세팅
                Object sortVal = extractSortValue(last, safeSortField); // 정렬 기준값 추출
                nextCursor = (sortVal == null) ? null : String.valueOf(sortVal); // 다음 커서값
                nextIdAfter = last.getId();                         // 다음 시작점 id
            } else {
                // ★ 변경 지점: 마지막 페이지 → Swagger 예시와 동일하게 null 반환
                nextCursor = null;                                   // (기존에는 "false" 문자열이었음) 삭제/변경
                nextIdAfter = null;                                   // (기존에는 마지막 id 유지 가능하게 했던 로직 제거)
            }
        }

        long total = autoSyncRepositoryCustom.countByFilters(indexInfoId, enabled); // Slice에는 count 없음 → 별도 조회

        return new CursorPageResponseAutoSyncConfigDto(               // 응답 DTO 조립
                content,                                              // 목록
                nextCursor,     // 마지막 페이지면 null
                nextIdAfter,    // 마지막 페이지면 null
                pageSize,                                             // 페이지 크기
                total,                                                // 총 개수
                hasNext                                               // 다음 페이지 존재 여부
        );
    }

    // ===== 유틸 – 정렬 필드/크기/커서 파싱/정렬값 추출 =====

    private int normalizeSize(Integer s) {                            // 페이지 크기 보정
        int v = (s == null ? 10 : s);                                 // 기본값 10
        if (v < 1) v = 1;                                             // 최소 1
        if (v > 100) v = 100;                                         // 최대 100
        return v;                                                     // 보정값 반환
    }

    private String normalizeSortField(String sortField) {              // 정렬 필드 화이트리스트
        if (!StringUtils.hasText(sortField)) return "indexInfo.indexName"; // 기본값
        if ("indexInfo.indexName".equals(sortField) || "enabled".equals(sortField)) { // 허용 목록
            return sortField;                                         // 통과
        }
        return "indexInfo.indexName";                                 // 비허용 시 기본값
    }

    private Object parseCursorValue(String cursor, String sortField) { // 커서 문자열을 정렬 타입에 맞게 파싱
        if (!StringUtils.hasText(cursor)) return null;                // 없으면 null
        if ("enabled".equals(sortField)) {                            // 불리언 정렬이면
            return Boolean.valueOf(cursor);                           // "true"/"false" → Boolean
        }
        return cursor;                                                // 문자열 정렬이면 그대로 반환
    }

    private Object extractSortValue(AutoSync e, String sortField) {   // 엔티티에서 정렬값 추출
        if ("enabled".equals(sortField)) {                            // enabled 케이스
            return Boolean.TRUE.equals(e.getEnabled());               // null-safe 불리언
        }
        IndexInfo info = e.getIndexInfoId();                          // 연관 엔티티 접근
        return (info == null || info.getIndexName() == null) ? "" : info.getIndexName(); // null-safe 문자열
    }
}
