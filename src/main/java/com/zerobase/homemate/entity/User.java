package com.zerobase.homemate.entity;

import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 프로필 닉네임
  @Column(name = "profile_name", length = 50)
  private String profileName;

  // 프로필 이미지 URL
  @Column(name = "profile_image_url", length = 512)
  private String profileImageUrl;

  // 권한: USER / ADMIN
  @Enumerated(EnumType.STRING)
  @Column(name = "user_role", nullable = false, length = 10)
  private UserRole userRole;

  // 사용자 상태: ACTIVE / SUSPENDED / DELETED
  @Enumerated(EnumType.STRING)
  @Column(name = "user_status", nullable = false, length = 12)
  private UserStatus userStatus;

  // 마지막 로그인 시각
  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  // 가입/수정/탈퇴 시각
  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void loginAndProfileUpdate(String newProfileName, String newProfileImageUrl, LocalDateTime now) {
    this.lastLoginAt = now;

    if (newProfileName != null && !newProfileName.equals(this.profileName)) {
      this.profileName = newProfileName;
    }
    if (newProfileImageUrl != null && !newProfileImageUrl.equals(this.profileImageUrl)) {
      this.profileImageUrl = newProfileImageUrl;
    }
  }

  public void withdraw() {
    this.userStatus = UserStatus.DELETED;
    this.deletedAt = LocalDateTime.now();
  }
}
