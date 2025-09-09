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

    private final AutoSyncRepository autoSyncRepository;
    private final AutoSyncMapper autoSyncMapper; // ★ Mapper 주입

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        int pageSize = normalizeSize(size);
        boolean asc = !"desc".equalsIgnoreCase(sortDirection);
        String safeSortField = normalizeSortField(sortField);

        List<AutoSync> rows = fetchByFilters(indexInfoId, enabled);
        long totalElements = rows.size();

        rows.sort(buildComparator(safeSortField, asc));

        CursorInfo lastCursor = decodeCursorOrIdAfter(cursor, idAfter, safeSortField);
        if (lastCursor != null) {
            rows = rows.stream()
                    .filter(e -> isAfterCursor(e, lastCursor, safeSortField, asc))
                    .collect(Collectors.toList());
        }

        boolean hasNext = rows.size() > pageSize;
        if (hasNext) {
            rows = rows.subList(0, pageSize);
        }

        // ★ 엔티티 → DTO 매핑을 Mapper로 변경
        List<AutoSyncConfigDto> content = autoSyncMapper.toDtoList(rows);

        // hasNext일 때만 다음 커서 생성 (마지막 페이지는 null)
        String nextCursor = null;
        if (hasNext && !content.isEmpty()) {
            Object sortValue = extractSortValue(rows.get(rows.size() - 1), safeSortField);
            nextCursor = encodeCursor(content.get(content.size() - 1).id(), sortValue);
        }

        return new CursorPageResponseAutoSyncConfigDto(
                content,
                nextCursor,
                nextCursor,
                pageSize,
                totalElements,
                hasNext
        );
    }

    // ===== 내부 유틸 (그대로) =====

    private int normalizeSize(Integer s) {
        int size = (s == null ? 10 : s);
        if (size < 1) size = 1;
        if (size > 100) size = 100;
        return size;
    }

    private String normalizeSortField(String sortField) {
        if (!StringUtils.hasText(sortField)) return "indexInfo.indexName";
        if ("indexInfo.indexName".equals(sortField) || "enabled".equals(sortField)) {
            return sortField;
        }
        return "indexInfo.indexName";
    }

    private static class CursorInfo {
        Object sortValue;
        Long id;
    }

    private CursorInfo decodeCursorOrIdAfter(String cursor, Long idAfter, String sortField) {
        CursorInfo ci = new CursorInfo();
        if (StringUtils.hasText(cursor)) {
            try {
                byte[] decoded = Base64.getUrlDecoder().decode(cursor);
                @SuppressWarnings("unchecked")
                Map<String, Object> map = MAPPER.readValue(decoded, Map.class);
                ci.id = map.get("id") instanceof Number n ? n.longValue() : Long.valueOf(map.get("id").toString());
                ci.sortValue = map.get("sort");
                return ci;
            } catch (Exception ignore) {
                return null;
            }
        }
        if (idAfter != null) {
            ci.id = idAfter;
            ci.sortValue = null;
            return ci;
        }
        return null;
    }

    private String encodeCursor(Long id, Object sortValue) {
        if (id == null) return null;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            if (sortValue != null) map.put("sort", sortValue);
            byte[] json = MAPPER.writeValueAsBytes(map);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            return null;
        }
    }

    private Object extractSortValue(AutoSync e, String sortField) {
        if ("enabled".equals(sortField)) {
            return Boolean.TRUE.equals(e.getEnabled());
        }
        if ("indexInfo.indexName".equals(sortField)) {
            IndexInfo info = e.getIndexInfoId();
            return info == null ? "" : (info.getIndexName() == null ? "" : info.getIndexName());
        }
        return null;
    }

    private boolean isAfterCursor(AutoSync e, CursorInfo c, String sortField, boolean asc) {
        Object val = extractSortValue(e, sortField);
        int cmp = 0;
        if (val instanceof String s1 && c.sortValue instanceof String s2) {
            cmp = s1.compareTo(s2);
        } else if (val instanceof Boolean b1 && c.sortValue instanceof Boolean b2) {
            cmp = Boolean.compare(b1, b2);
        }
        if (asc) {
            return cmp > 0 || (cmp == 0 && e.getId() > c.id);
        } else {
            return cmp < 0 || (cmp == 0 && e.getId() < c.id);
        }
    }

    private List<AutoSync> fetchByFilters(Long indexInfoId, Boolean enabled) {
        if (indexInfoId != null && enabled != null) {
            return autoSyncRepository.findByIndexInfoId_IdAndEnabled(indexInfoId, enabled);
        }
        if (indexInfoId != null) {
            return autoSyncRepository.findByIndexInfoId_Id(indexInfoId);
        }
        if (enabled != null) {
            return autoSyncRepository.findByEnabled(enabled);
        }
        return autoSyncRepository.findAll();
    }

    private Comparator<AutoSync> buildComparator(String sortField, boolean asc) {
        Comparator<AutoSync> primary;
        if ("enabled".equals(sortField)) {
            primary = Comparator.comparing(a -> Boolean.TRUE.equals(a.getEnabled()));
        } else {
            primary = Comparator.comparing(a -> {
                IndexInfo info = a.getIndexInfoId();
                return info == null ? "" : (info.getIndexName() == null ? "" : info.getIndexName());
            }, Comparator.naturalOrder());
        }
        if (!asc) primary = primary.reversed();

        Comparator<AutoSync> byId = Comparator.comparing(AutoSync::getId);
        if (!asc) byId = byId.reversed();

        return primary.thenComparing(byId);
    }
}
