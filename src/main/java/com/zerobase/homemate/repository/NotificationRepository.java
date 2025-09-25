package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUser_IdAndCategory(Long userId, Notification.Category category);
}
