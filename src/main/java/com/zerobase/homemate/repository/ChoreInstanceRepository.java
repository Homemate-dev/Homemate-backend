package com.zerobase.homemate.repository;

import com.zerobase.homemate.chore.dto.ChoreStatusCountDto;
import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.enums.ChoreStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChoreInstanceRepository extends JpaRepository<ChoreInstance, Long> {

    @EntityGraph(attributePaths = {"chore"})
    Optional<ChoreInstance> findByIdAndChore_User_Id(Long id, Long userId);

    @Query("""
                SELECT ci FROM ChoreInstance ci
                JOIN FETCH ci.chore
                WHERE ci.chore.user.id = :userId
                  AND ci.dueDate = :date
                  AND ci.choreStatus IN (
                      com.zerobase.homemate.entity.enums.ChoreStatus.PENDING,
                      com.zerobase.homemate.entity.enums.ChoreStatus.COMPLETED
                  )
                ORDER BY ci.notificationTime ASC, ci.id ASC
            """)
    List<ChoreInstance> findChoreInstancesByDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    List<ChoreInstance> findByChoreAndChoreStatus(Chore chore, ChoreStatus choreStatus);

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
            @Param("included") Collection<ChoreStatus> included
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE ChoreInstance ci
                   SET ci.choreStatus = com.zerobase.homemate.entity.enums.ChoreStatus.DELETED,
                       ci.deletedAt = CURRENT_TIMESTAMP
                 WHERE ci.chore = :chore
            """)
    void bulkSoftDeleteByChore(
            @Param("chore") Chore chore
    );

    @Query("""
                select new com.zerobase.homemate.chore.dto.ChoreStatusCountDto(
                    ci.chore.id,
                    sum(case when ci.choreStatus = com.zerobase.homemate.entity.enums.ChoreStatus.PENDING then 1 else 0 end),
                    sum(case when ci.choreStatus = com.zerobase.homemate.entity.enums.ChoreStatus.COMPLETED then 1 else 0 end)
                )
                from ChoreInstance ci
                where ci.chore.id in :choreIds
                group by ci.chore.id
            """)
    List<ChoreStatusCountDto> countPendingCompletedByChoreIds(
            @Param("choreIds") List<Long> choreIds
    );
}
