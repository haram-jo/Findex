package com.codeit.findex.service.basic;

import com.codeit.findex.dto.data.AutoSyncConfigDto;
import com.codeit.findex.dto.data.CursorPageResponseAutoSyncConfigDto;
import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.mapper.AutoSyncMapper;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.service.AutoSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicAutoSyncService implements AutoSyncService {

    private final AutoSyncRepository autoSyncRepository; // AutoSync 엔티티용 JPA Repository
    private final AutoSyncMapper autoSyncMapper;         // AutoSync 엔티티 <-> DTO 변환을 담당하는 Mapper

    private static final ObjectMapper MAPPER = new ObjectMapper(); // 커서 인코딩/디코딩용 JSON 처리기

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

        // 커서 or idAfter 조건 해석
        CursorInfo lastCursor = decodeCursorOrIdAfter(cursor, idAfter, safeSortField);
        if (lastCursor != null) {
            // 커서 이후 데이터만 필터링
            rows = rows.stream()
                    .filter(e -> isAfterCursor(e, lastCursor, safeSortField, asc))
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
        if (hasNext && !content.isEmpty()) {
            Object sortValue = extractSortValue(rows.get(rows.size() - 1), safeSortField);
            nextCursor = encodeCursor(content.get(content.size() - 1).id(), sortValue);
        }

        // 커서 페이지 응답 DTO 생성 후 반환
        return new CursorPageResponseAutoSyncConfigDto(
                content,      // 현재 페이지 데이터
                nextCursor,   // 다음 커서
                nextCursor,   // prevCursor도 동일하게 설정 (필요 시 분리 가능)
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
     * 예)
     *  sortField = "indexInfo.indexName" 일 때
     *    sortValue = "KOSPI 200"
     *  혹은
     *  sortField = "enabled" 일 때
     *    sortValue = true/false
     */
    private static class CursorInfo {
        Object sortValue; // 동적 타입 (String 또는 Boolean 등)
        Long id;          // 마지막 아이템의 PK
    }

    /**
     * 커서 문자열(Base64) 또는 idAfter 파라미터를 CursorInfo 로 변환.
     *
     * 우선순위:
     *  1) cursor(String) 가 유효하면 cursor 사용
     *  2) cursor 가 없고 idAfter 가 있으면 idAfter 사용
     *  3) 둘 다 없으면 null (첫 페이지)
     *
     * cursor 포맷:
     *  - Base64 URL-safe 로 인코딩된 JSON
     *  - 예: {"id": 123, "sort": "KOSPI 200"} 또는 {"id": 123, "sort": true}
     *
     * @param cursor   Base64 URL-safe 커서 문자열 (nullable)
     * @param idAfter  마지막으로 본 항목의 id (nullable) — cursor 대비 단순 모드
     * @param sortField 현재 정렬 필드 (문맥상 필요하지만 여기선 보관용)
     * @return CursorInfo (없으면 null)
     */
    private CursorInfo decodeCursorOrIdAfter(String cursor, Long idAfter, String sortField) {
        CursorInfo ci = new CursorInfo();
        if (StringUtils.hasText(cursor)) {
            try {
                // 1) Base64 URL-safe 디코드 → 2) JSON 파싱 → 3) map 추출
                byte[] decoded = Base64.getUrlDecoder().decode(cursor);
                @SuppressWarnings("unchecked")
                Map<String, Object> map = MAPPER.readValue(decoded, Map.class);

                // id 는 Number 또는 String 로 올 수 있으므로 안전하게 Long 변환
                ci.id = map.get("id") instanceof Number n
                        ? n.longValue()
                        : Long.valueOf(map.get("id").toString());

                // sortValue 는 타입이 가변적(Boolean/String) — 그대로 저장
                ci.sortValue = map.get("sort");
                return ci; // 정상 커서 해석
            } catch (Exception ignore) {
                // 잘못된 커서 포맷이면 null 로 처리 (첫 페이지로 간주)
                return null;
            }
        }
        // cursor 없으면 idAfter 사용 (단순히 PK 기준으로 페이징)
        if (idAfter != null) {
            ci.id = idAfter;
            ci.sortValue = null; // sortValue 없이 id 기준만 사용
            return ci;
        }
        return null; // 커서/아이디 기준 없음 → 첫 페이지
    }

    /**
     * CursorInfo 를 Base64 URL-safe 문자열로 직렬화.
     * - 응답의 nextCursor 로 내려보내 클라이언트가 이어서 요청 가능
     *
     * 직렬화 규칙:
     *  - JSON 생성: {"id": <Long>, "sort": <Object|null>}
     *  - Base64 URL-safe + padding 제거
     *
     * @param id        마지막 아이템의 PK (필수)
     * @param sortValue 마지막 아이템의 정렬 기준 값 (nullable)
     * @return Base64 URL-safe 커서 문자열 (실패/입력없음 시 null)
     */
    private String encodeCursor(Long id, Object sortValue) {
        if (id == null) return null; // id 없으면 커서 의미가 없음
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            if (sortValue != null) map.put("sort", sortValue);
            byte[] json = MAPPER.writeValueAsBytes(map); // JSON 직렬화
            // URL-safe Base64 + padding 제거 (쿼리스트링에 안전하게 넣기 위함)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            return null; // 예외 시 커서 미제공 (클라이언트는 다음 페이지 없음으로 인식)
        }
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
     * @param c         커서 (마지막으로 본 항목의 sortValue + id)
     * @param sortField 현재 정렬 필드
     * @param asc       오름차순 여부 (false 면 내림차순)
     * @return true 이면 "e 는 커서 이후 데이터" → 페이지 결과에 포함 가능
     */
    private boolean isAfterCursor(AutoSync e, CursorInfo c, String sortField, boolean asc) {
        Object val = extractSortValue(e, sortField); // 현재 행의 정렬값
        int cmp = 0;

        // 타입별 비교 (String, Boolean 만 지원)
        if (val instanceof String s1 && c.sortValue instanceof String s2) {
            cmp = s1.compareTo(s2); // 문자열 사전순 비교
        } else if (val instanceof Boolean b1 && c.sortValue instanceof Boolean b2) {
            cmp = Boolean.compare(b1, b2); // false < true 규칙
        }
        // asc: 정방향 → (정렬값이 더 크거나) 정렬값이 같으면 id 가 더 커야 "이후"
        // desc: 역방향 → (정렬값이 더 작거나) 정렬값이 같으면 id 가 더 작아야 "이후"
        if (asc) {
            return cmp > 0 || (cmp == 0 && e.getId() > c.id);
        } else {
            return cmp < 0 || (cmp == 0 && e.getId() < c.id);
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
