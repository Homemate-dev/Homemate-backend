package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.enums.NotificationCategory;
import com.zerobase.homemate.entity.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: User, Chore, ChoreInstance 연관관계 추가
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "chore_id", nullable = false)
    private Long choreId;
    @Column(name = "chore_instance_id", nullable = false)
    private Long choreInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_category", nullable = false)
    private NotificationCategory notificationCategory;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false)
    private NotificationStatus notificationStatus;

    @Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN")
    private Boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
