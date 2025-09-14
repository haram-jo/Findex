package com.codeit.findex.dto.data;

import lombok.Builder;

@Builder
public record IndexInfoUnique(
    String indexClassification,
    String indexName
) {}
