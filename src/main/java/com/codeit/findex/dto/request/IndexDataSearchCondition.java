package com.codeit.findex.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Base64;

public record IndexDataSearchCondition(
    Long indexInfoId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {
    public IndexDataSearchCondition {
        if (sortField == null || sortField.isBlank()) {
            sortField = "baseDate";
        }
        if (sortDirection == null || sortDirection.isBlank()) {
            sortDirection = "desc";
        }
        if (size == null) {
            size = 10;
        }
    }

    public Long getResolvedId() {
        if (cursor != null && !cursor.isBlank()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(cursor));
                return Long.parseLong(decoded.replaceAll("[^0-9]", ""));
            } catch (Exception e) {
                return null;
            }
        }
        return idAfter;
    }
}