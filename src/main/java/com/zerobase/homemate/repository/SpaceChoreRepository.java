package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Space;
import com.zerobase.homemate.entity.SpaceChore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaceChoreRepository extends JpaRepository<SpaceChore, Long> {

    // 공간별 활성화된 집안일 조회
    List<SpaceChore> findBySpaceAndIsActiveTrue(Space space);

    // 랜덤 추천 4개 조회 (JPQL 또는 native query 필요)
    List<SpaceChore> findTop4BySpaceAndIsActiveTrueOrderByRand(Space space); // MySQL 기준
}
