package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Notification;
import com.zerobase.homemate.entity.enums.NotificationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserId(Long userId);

    List<Notification> findAllByUserIdAndNotificationCategory(Long userId, NotificationCategory notificationCategory);
}