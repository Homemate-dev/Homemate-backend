package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
