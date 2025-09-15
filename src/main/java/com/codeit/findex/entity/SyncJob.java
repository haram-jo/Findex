package com.codeit.findex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "sync_jobs")
public class SyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "index_info_id", nullable = false)
    private  IndexInfo indexInfo;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private LocalDate targetDate;

    private String worker;

    private LocalDateTime jobTime;

    private Boolean result;

    public SyncJob update(LocalDateTime jobTime, String worker, Boolean result) {
        this.jobTime = jobTime;
        this.worker = worker;
        this.result = result;
        return this;
    }

    public void setResult(ResultType resultType) {
        this.result = resultType.toBoolean();
    }

}
