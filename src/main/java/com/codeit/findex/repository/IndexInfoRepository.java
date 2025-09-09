package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/*수정되면 말하기
*/

// <Entity, PK타입> 상속
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {
}