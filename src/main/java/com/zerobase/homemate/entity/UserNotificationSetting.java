package com.zerobase.homemate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_notification_settings")
public class UserNotificationSetting {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "first_setup_completed", nullable = false)
  private boolean firstSetupCompleted;

  @Column(name = "master_enabled", nullable = false)
  private boolean masterEnabled;

  @Column(name = "housework_enabled", nullable = false)
  private boolean houseworkEnabled;

  @Column(name = "notice_enabled", nullable = false)
  private boolean noticeEnabled;

  @Column(name = "notification_time", columnDefinition = "TIME", nullable = false)
  private LocalTime notificationTime;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public static UserNotificationSetting createDefault(User user, LocalTime defaultTime) {
    return UserNotificationSetting.builder()
        .user(user)
        .firstSetupCompleted(false)
        .masterEnabled(true)
        .houseworkEnabled(true)
        .noticeEnabled(true)
        .notificationTime(defaultTime)
        .build();
  }
}
