package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChoreNotificationRepository extends JpaRepository<ChoreNotification, Long> {

    // 정렬과 limit는 Pageable을 통해 수행
    @Query("""
            SELECT cn from ChoreNotification cn
            WHERE cn.user.id = :userId
            AND cn.isCancelled = false
            AND cn.scheduledAt <= :scheduledAt
            """)
    List<ChoreNotification> findChoreNotifications(
            @Param("userId") Long userId,
            @Param("scheduledAt") LocalDateTime scheduledAt,
            Pageable pageable
    );
}