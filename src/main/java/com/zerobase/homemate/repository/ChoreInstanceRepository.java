package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreInstanceRepository extends JpaRepository<ChoreInstance, Long> {

    List<ChoreInstance> findByChoreIdAndDueDateGreaterThanEqualAndChoreStatus(
        Long choreId,
        LocalDate dueDate,
        ChoreStatus choreStatus
    );

    @EntityGraph(attributePaths = "chore")
    List<ChoreInstance> findAllByChore_User_IdAndDueDateAndChoreStatusInOrderByNotificationTimeAscIdAsc(
        Long userId, LocalDate date, Collection<ChoreStatus> included);

    @Query("""
        SELECT ci.dueDate
        FROM ChoreInstance ci
        WHERE ci.chore.user.id = :userId
          AND ci.chore.isDeleted = false
          AND ci.dueDate BETWEEN :start AND :end
          AND ci.choreStatus IN :included
        GROUP BY ci.dueDate
        ORDER BY ci.dueDate ASC
    """)
    List<LocalDate> findDatesHavingInstances(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end,
        @Param("included") Collection<ChoreStatus> included);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ChoreInstance ci
            SET ci.choreStatus = :deleted,
                ci.deletedAt = :now
        WHERE ci.chore.id = :choreId
          AND ci.choreStatus IN :included
    """)
    void bulkSoftDeleteByChoreIdAndStatuses(
        @Param("choreId") Long choreId,
        @Param("included") Collection<ChoreStatus> statuses,
        @Param("deleted") ChoreStatus deleted,
        @Param("now") LocalDateTime now);
}
