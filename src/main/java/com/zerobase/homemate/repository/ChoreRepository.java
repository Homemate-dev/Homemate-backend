package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import java.util.List;

import com.zerobase.homemate.entity.enums.RepeatType;
import com.zerobase.homemate.entity.enums.Space;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

    List<Chore> findByUserIdAndIsDeletedIsFalse(Long userId, Sort sort);

    List<Chore> findByUserIdAndSpaceAndIsDeletedIsFalse(
            Long userId, Space space, Sort sort);

    List<Chore> findByUserIdAndRepeatTypeAndRepeatIntervalAndIsDeletedIsFalse(
            Long userId, RepeatType repeatType, int repeatInterval, Sort sort);
}
