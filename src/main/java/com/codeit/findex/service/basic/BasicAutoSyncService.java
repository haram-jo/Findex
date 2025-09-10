package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.AutoSyncConfigDto;
import com.codeit.findex.dto.data.CursorPageResponseAutoSyncConfigDto;
import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.mapper.AutoSyncMapper;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.service.AutoSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicAutoSyncService implements AutoSyncService {

    private final AutoSyncRepository autoSyncRepository; // AutoSync 엔티티용 JPA Repository
    private final AutoSyncMapper autoSyncMapper;         // AutoSync 엔티티 <-> DTO 변환을 담당하는 Mapper

    // ====== PATCH 구현 추가 ======
    @Transactional
    @Override
    public AutoSyncConfigDto updateEnabled(Long id, Boolean enabled) {
        // 요청값 null 체크: enabled가 반드시 있어야 함
        if (enabled == null) {
            throw new IllegalArgumentException("`enabled` must not be null.");
        }

        // id로 엔티티 조회, 없으면 예외 발생
        AutoSync entity = autoSyncRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AutoSync not found: " + id));

        // 엔티티의 enabled 값을 요청값으로 변경
        // JPA Dirty Checking 덕분에 트랜잭션 종료 시점에 DB UPDATE 쿼리가 자동 실행됨
        entity.setEnabled(enabled);

        // 변경된 엔티티를 DTO로 변환하여 반환
        return autoSyncMapper.toDto(entity);
    }

    // ====== 목록 조회 구현 ======
    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseAutoSyncConfigDto list(
            Long indexInfoId,
            Boolean enabled,
            Long idAfter,
            String cursor,
            String sortField,
            String sortDirection,
            Integer size
    ) {
        // 페이지 크기 정규화 (1~100 사이만 허용)
        int pageSize = normalizeSize(size);

        // 정렬 방향 (기본 asc, desc일 경우만 반전)
        boolean asc = !"desc".equalsIgnoreCase(sortDirection);

        // 정렬 필드 정규화 (허용된 필드만, 기본값 indexInfo.indexName)
        String safeSortField = normalizeSortField(sortField);

        // 조건에 맞는 전체 데이터 조회
        List<AutoSync> rows = fetchByFilters(indexInfoId, enabled);
        long totalElements = rows.size(); // 전체 개수

        // 정렬 적용
        rows.sort(buildComparator(safeSortField, asc));

        // 커서 or idAfter 조건 해석 (문자열/불리언 + id)
        CursorInfo last = parseCursor(cursor, idAfter, safeSortField);
        if (last != null) {
            // 커서 이후 데이터만 필터링
            rows = rows.stream()
                    .filter(e -> isAfterCursor(e, last, safeSortField, asc))
                    .collect(Collectors.toList());
        }

        // 다음 페이지 여부 판단
        boolean hasNext = rows.size() > pageSize;
        if (hasNext) {
            rows = rows.subList(0, pageSize); // 다음 페이지가 있으면 pageSize 만큼만 자르기
        }

        // 엔티티 리스트 -> DTO 리스트 변환
        List<AutoSyncConfigDto> content = autoSyncMapper.toDtoList(rows);

        // 다음 커서 생성 (마지막 페이지일 경우 null)
        String nextCursor = null;
        Long nextIdAfter = null;

        if (hasNext && !content.isEmpty()) {
            AutoSync lastRow = rows.get(rows.size() - 1);
            Object sortValue = extractSortValue(lastRow, safeSortField);
            nextCursor = (sortValue == null) ? null : String.valueOf(sortValue);
            nextIdAfter = lastRow.getId();
        }

        // 커서 페이지 응답 DTO 생성 후 반환
        return new CursorPageResponseAutoSyncConfigDto(
                content,      // 현재 페이지 데이터
                nextCursor,   // 다음 커서(정렬값 자체)
                nextIdAfter,   // 다음 시작점 id
                pageSize,     // 페이지 크기
                totalElements,// 전체 데이터 개수
                hasNext       // 다음 페이지 존재 여부
        );
    }
    // ===== 내부 유틸 (커서/정렬/필터링 처리) =====

    /**
     * 페이지 크기 정규화 유틸.
     * - null 이면 기본값 10
     * - 최소 1, 최대 100 으로 강제 (API 남용/오입력 방지)
     *
     * @param s 요청으로 들어온 size (nullable)
     * @return 1~100 범위의 합법적인 page size
     */
    private int normalizeSize(Integer s) {
        int size = (s == null ? 10 : s); // null 이면 기본 10
        if (size < 1) size = 1;          // 하한 보정
        if (size > 100) size = 100;      // 상한 보정 (서버 보호)
        return size;
    }

    /**
     * 정렬 필드 정규화.
     * - 허용 목록: "indexInfo.indexName", "enabled"
     * - 그 외가 들어오면 안전한 기본값 "indexInfo.indexName" 으로 강제
     *
     * @param sortField 클라이언트가 요구한 정렬 필드
     * @return 허용된 정렬 필드 중 하나
     */
    private String normalizeSortField(String sortField) {
        if (!StringUtils.hasText(sortField)) return "indexInfo.indexName"; // 미입력 시 기본
        if ("indexInfo.indexName".equals(sortField) || "enabled".equals(sortField)) {
            return sortField; // 화이트리스트 체크
        }
        return "indexInfo.indexName"; // 비허용 값 방어 코드
    }

    /**
     * 커서 정보를 담기 위한 내부 전용 DTO.
     * - sortValue: 정렬 기준 필드의 마지막 값 (문자열/불리언 등)
     * - id: tie-breaker 로 사용되는 PK (동일 sortValue 내에서 다음/이전 경계 판단)
     *
     * 스펙 준수:
     *  - cursor(문자열) = 정렬값 자체 (예: indexName → "보험", enabled → "true"/"false")
     *  - idAfter(Long)  = 마지막 요소의 id
     */
    private static class CursorInfo {
        Object sortValue; // 동적 타입 (String 또는 Boolean 등)
        Long id;          // 마지막 아이템의 PK
    }

    /**
     * 클라이언트로부터 받은 "cursor(정렬값 자체)"와 "idAfter(마지막 id)"를 CursorInfo로 파싱.
     * Base64 사용하지 않고, 스펙 그대로 문자열/불리언을 해석한다.
     *
     * @param cursor    정렬값 자체 (예: "보험" 또는 "true"/"false"), nullable
     * @param idAfter   마지막 id (nullable)
     * @param sortField 현재 정렬 필드 ("indexInfo.indexName" | "enabled")
     * @return CursorInfo or null(첫 페이지)
     */
    private CursorInfo parseCursor(String cursor, Long idAfter, String sortField) {
        if (!StringUtils.hasText(cursor) && idAfter == null) {
            return null; // 첫 페이지
        }
        CursorInfo ci = new CursorInfo();
        ci.id = idAfter; // idAfter는 있는 그대로 사용

        if("enabled".equals(sortField)) {
            // "true"/"false" 문자열을 Boolean으로 해석
            ci.sortValue = StringUtils.hasText(cursor) ? Boolean.valueOf(cursor) : null;
        } else {
            // indexName 정렬인 경우: 문자열 그대로 사용
            ci.sortValue = cursor; // null 허용
        }
        return ci;
    }

    /**
     * 정렬 필드에 해당하는 값을 엔티티에서 꺼내는 함수.
     * - sortField 가 "enabled" 면 Boolean 값 반환
     * - sortField 가 "indexInfo.indexName" 면 String 값 반환
     * - 그 외는 null
     *
     * @param e         AutoSync 엔티티
     * @param sortField 현재 정렬 필드
     * @return 정렬 비교에 사용할 값 (Boolean/String)
     */
    private Object extractSortValue(AutoSync e, String sortField) {
        if ("enabled".equals(sortField)) {
            // Boolean 의 null 안전 비교를 위해 Boolean.TRUE.equals 사용
            return Boolean.TRUE.equals(e.getEnabled());
        }
        if ("indexInfo.indexName".equals(sortField)) {
            // 연관 객체 null-safe 접근: IndexInfo 또는 indexName 이 null 일 수 있음
            IndexInfo info = e.getIndexInfoId();
            return info == null ? "" : (info.getIndexName() == null ? "" : info.getIndexName());
        }
        return null;
    }

    /**
     * "현재 행 e" 가 "커서 c" 이후인지 판별.
     * - 문자열 정렬/불리언 정렬 모두 지원
     * - asc/desc 두 방향 모두 고려
     * - 같은 sortValue 에서는 id 로 2차 비교하여 경계 결정 (안정적 페이지네이션)
     *
     * @param e         현재 행
     * @param last         커서 (마지막으로 본 항목의 sortValue + id)
     * @param sortField 현재 정렬 필드
     * @param asc       오름차순 여부 (false 면 내림차순)
     * @return true 이면 "e 는 커서 이후 데이터" → 페이지 결과에 포함 가능
     */
    private boolean isAfterCursor(AutoSync e, CursorInfo last, String sortField, boolean asc) {
        // 커서가 완전히 비어있으면 필터링하지 않음
        if (last.sortValue == null && last.id == null) return true;

        Object val = extractSortValue(e, sortField); // 현재 행의 정렬값
        int cmp = 0;

        // 타입별 비교 (String, Boolean 만 지원)
        if (val instanceof String s1 && last.sortValue instanceof String s2) {
            cmp = s1.compareTo(s2); // 문자열 사전순 비교
        } else if (val instanceof Boolean b1 && last.sortValue instanceof Boolean b2) {
            cmp = Boolean.compare(b1, b2); // false < true 규칙
        } else if (last.sortValue == null) {
            // 커서가 완전히 비어있으면 필터링하지 않음
            if (asc) return e.getId() > (last.id == null ? Long.MIN_VALUE : last.id);
            return e.getId() < (last.id == null ? Long.MAX_VALUE : last.id);
        }
        // asc: 정방향 → (정렬값이 더 크거나) 정렬값이 같으면 id 가 더 커야 "이후"
        // desc: 역방향 → (정렬값이 더 작거나) 정렬값이 같으면 id 가 더 작아야 "이후"
        if (asc) {
            return cmp > 0 || (cmp == 0 && e.getId() > last.id);
        } else {
            return cmp < 0 || (cmp == 0 && e.getId() < last.id);
        }
    }

    /**
     * 필터 조합에 따라 Repository 메서드를 선택 호출.
     * - indexInfoId & enabled 동시 적용
     * - indexInfoId 만 적용
     * - enabled 만 적용
     * - 아무 필터 없으면 전체 조회
     *
     * @param indexInfoId 지수 정보 ID (nullable)
     * @param enabled     활성화 여부 (nullable)
     * @return 필터링된 AutoSync 목록
     */
    private List<AutoSync> fetchByFilters(Long indexInfoId, Boolean enabled) {
        if (indexInfoId != null && enabled != null) {
            // 두 필터 모두 활성화: WHERE index_info_id = ? AND enabled = ?
            return autoSyncRepository.findByIndexInfoId_IdAndEnabled(indexInfoId, enabled);
        }
        if (indexInfoId != null) {
            // 지수 ID 필터만: WHERE index_info_id = ?
            return autoSyncRepository.findByIndexInfoId_Id(indexInfoId);
        }
        if (enabled != null) {
            // 활성화 여부만: WHERE enabled = ?
            return autoSyncRepository.findByEnabled(enabled);
        }
        // 필터 없음: 전체
        return autoSyncRepository.findAll();
    }

    /**
     * 정렬 필드 + 정렬 방향을 기반으로 Comparator 생성.
     * - 1차 키: enabled 또는 indexInfo.indexName
     * - 2차 키: id (항상 동일 방향) → 정렬 안정성 확보 (동일 sortValue 내에서 일관된 순서)
     *
     * Comparator 체이닝 포인트:
     *  - primary: 지정된 필드로 우선 정렬
     *  - byId: tie-breaker 로 PK 사용
     *
     * @param sortField 정렬 필드 ("enabled" | "indexInfo.indexName")
     * @param asc       오름차순 여부
     * @return 합성 Comparator
     */
    private Comparator<AutoSync> buildComparator(String sortField, boolean asc) {
        Comparator<AutoSync> primary;

        if ("enabled".equals(sortField)) {
            // enabled: false < true 기준으로 정렬
            primary = Comparator.comparing(a -> Boolean.TRUE.equals(a.getEnabled()));
        } else {
            // indexInfo.indexName: null-safe 로 빈 문자열 대체 후 사전순
            primary = Comparator.comparing(a -> {
                IndexInfo info = a.getIndexInfoId();
                return info == null ? "" : (info.getIndexName() == null ? "" : info.getIndexName());
            }, Comparator.naturalOrder());
        }

        // 내림차순 요청이면 1차 Comparator 반전
        if (!asc) primary = primary.reversed();

        // 2차 키: id (동일 sortValue 내에서 안정적 순서 보장)
        Comparator<AutoSync> byId = Comparator.comparing(AutoSync::getId);
        if (!asc) byId = byId.reversed(); // 요청 방향에 맞춰 보조 키도 반전

        // 합성: 1차 정렬 후, 동률이면 id 로 재정렬
        return primary.thenComparing(byId);
    }

}
