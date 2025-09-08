package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository
// <Entity, PK타입> 상속
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {
}