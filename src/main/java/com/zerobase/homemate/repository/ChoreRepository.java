package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

    // 사용자별 chore 조회
    List<Chore> findByUserIdAndIsDeletedFalse(Long userId);

    // SpaceChore 기준으로 사용자 chore 조회
    List<Chore> findBySpaceChoreAndIsDeletedFalse(SpaceChore spaceChore);

    // 특정 날짜 범위에 속하는 chore 조회 (반복 주기 계산용)
    List<Chore> findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsDeletedFalse(
            Long userId, java.time.LocalDate start, java.time.LocalDate end);
}
