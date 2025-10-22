package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.UserNotificationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
  boolean existsByUserId(Long userId);
  Optional<UserNotificationSetting> findByUserId(Long userId);

  Optional<UserNotificationSetting> findByUser(User user);
}
