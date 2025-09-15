package com.codeit.findex.dto.request;

public record IndexInfoSearchRequest(
        String indexClassification,
        String indexName,
        Boolean favorite,
        Long idAfter,
        String cursor,
        String sortField,
        String sortDirection,
        Integer size
) {
    public IndexInfoSearchRequest {
        if (sortField == null || sortField.trim().isEmpty()) sortField = "indexClassification";
        if (sortDirection == null || sortDirection.trim().isEmpty()) sortDirection = "asc";
        if (size == null) size = 10;
    }
}
