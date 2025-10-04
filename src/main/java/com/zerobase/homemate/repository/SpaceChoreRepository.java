package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Space;
import com.zerobase.homemate.entity.SpaceChore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpaceChoreRepository extends JpaRepository<SpaceChore, Long> {

    // 공간별 활성화된 집안일 조회
    List<SpaceChore> findBySpaceAndIsActiveTrue(Space space);

}
