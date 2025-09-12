package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import com.codeit.findex.repository.custom.DashBoardRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashBoardRepository extends JpaRepository<IndexData, Long>, DashBoardRepositoryCustom {
}
