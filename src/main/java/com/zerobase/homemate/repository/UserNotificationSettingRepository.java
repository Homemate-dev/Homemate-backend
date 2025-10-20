package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.UserNotificationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
  boolean existsByUserId(Long userId);
  Optional<UserNotificationSetting> findByUserId(Long userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      UPDATE UserNotificationSetting s
         SET s.masterEnabled = TRUE,
             s.choreEnabled = TRUE,
             s.noticeEnabled = TRUE
       WHERE s.user.id = :userId
         AND s.masterEnabled = FALSE
  """)
  void enableUserNotificationSetting(@Param("userId") Long userId);

}
