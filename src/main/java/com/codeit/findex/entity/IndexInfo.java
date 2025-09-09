package com.codeit.findex.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "index_infos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // UUID id

  private String indexClassification; // 지수 분류명
  private String indexName;           // 지수명
  private Integer employedItemsCount; // 채용종목수

  private String basePointInTime;     // 기준 시점(YYYY-MM-DD)
  private Double baseIndex;           // 기준 지수

  @Enumerated(EnumType.STRING)
  private SourceType sourceType;      // 출처 (User가 등록)

  private Boolean favorite;           // 즐겨찾기 여부
}
