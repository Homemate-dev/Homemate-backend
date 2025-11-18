package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.mypage.query.dto.MyPageDto;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
    SELECT user.id
    FROM User user
    WHERE user.userStatus = :status
      AND user.userRole = :userRole
    """)
    List<Long> findIdsByUserStatusAndUserRole(
        @Param("status") UserStatus status,
        @Param("userRole") UserRole userRole);

    @Query("""
    SELECT new com.zerobase.homemate.mypage.query.dto.MyPageDto(
        u.id, sa.socialProvider, u.profileName, u.profileImageUrl,
        u.createdAt, s.updatedAt, u.lastLoginAt,
        s.masterEnabled, s.choreEnabled, s.noticeEnabled,
        s.notificationTime
    )
    FROM User u
    LEFT JOIN UserSocialAccount sa ON sa.user = u
    LEFT JOIN UserNotificationSetting s ON s.user = u
    WHERE u.id = :id
    """)
    Optional<MyPageDto> findMyPageById(
        @Param("id") Long id
    );
}
