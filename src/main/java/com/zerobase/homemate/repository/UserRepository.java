package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.entity.enums.UserRole;
import com.zerobase.homemate.entity.enums.UserStatus;
import com.zerobase.homemate.mypage.query.dto.MyPageDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
            SELECT u.id FROM User u
            WHERE u.userStatus = :status
            AND u.userRole = :userRole
            """)
    List<Long> findIdsByUserStatusAndUserRole(
            @Param("status") UserStatus status,
            @Param("userRole") UserRole userRole
    );

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

    @Query("""
            SELECT u.id FROM User u
            WHERE u.userStatus = 'DELETED'
            AND u.deletedAt <= :threshold
            AND u.profileName IS NOT NULL
            AND u.profileImageUrl IS NOT NULL
            """)
    List<Long> findIdsToDelete(
            @Param("threshold") LocalDateTime threshold
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE User u
            SET u.profileName = null, u.profileImageUrl = null
            WHERE u.id IN :ids
            """)
    void deleteProfileData(
            @Param("ids") List<Long> ids
    );
}
