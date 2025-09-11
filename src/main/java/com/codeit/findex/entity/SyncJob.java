package com.codeit.findex.entity;

import com.codeit.findex.service.basic.BasicSyncJobService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "sync_jobs")
public class SyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private LocalDate targetDate;

    private String worker;

    private Instant jobTime;

    private BasicSyncJobService.ResultType result;

    @ManyToOne
    @JoinColumn(name = "index_info_id", nullable = false)
    private  IndexInfo indexInfo;

}
