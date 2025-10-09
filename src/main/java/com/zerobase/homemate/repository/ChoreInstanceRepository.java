package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreInstanceRepository extends JpaRepository<ChoreInstance, Long> {

    List<ChoreInstance> findByChoreIdAndDueDateGreaterThanEqualAndChoreStatus(
        Long choreId,
        LocalDate dueDate,
        ChoreStatus choreStatus
    );

    @EntityGraph(attributePaths = "chore")
    List<ChoreInstance> findAllByChore_User_IdAndDueDateAndChoreStatusNotInOrderByNotificationTimeAscIdAsc(
        Long userId, LocalDate date, Collection<ChoreStatus> excluded);
}
