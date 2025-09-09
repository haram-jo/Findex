package com.codeit.findex.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auto_sync")
public class AutoSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY) // 다:1 (지수 정보 기준으로 여러 설정 허용)
    @JoinColumn(name = "index_info_Id", nullable = false) // FK
    private IndexInfo indexInfoId; // 대상 지수 정보

    @Column(name = "enabled", nullable = false) // NOT NULL
    private Boolean enabled; // 활성화 여부
}
