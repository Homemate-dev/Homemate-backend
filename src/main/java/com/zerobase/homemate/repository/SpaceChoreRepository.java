package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.Space;
import com.zerobase.homemate.entity.SpaceChore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceChoreRepository extends JpaRepository<SpaceChore, Long> {

    // 공간별 활성화된 집안일 조회
    List<SpaceChore> findBySpaceAndIsActiveTrue(Space space);

//    Optional<SpaceChore> findBySpaceAndChore(Space space, Chore chore);
}
