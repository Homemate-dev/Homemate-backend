package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreNotification;
import com.zerobase.homemate.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChoreNotificationRepository extends JpaRepository<ChoreNotification, Long> {

    // 정렬과 limit는 Pageable을 통해 수행
    List<ChoreNotification> findByUserAndIsCancelledFalseAndScheduledAtBefore(
            User user,
            LocalDateTime scheduledAt,
            Pageable pageable
    );
}