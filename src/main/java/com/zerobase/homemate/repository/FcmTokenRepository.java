package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.FcmToken;
import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.notification.push.dto.TokenWithIdDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAllByUserAndIsActiveTrue(User user);

    @Query("""
            SELECT new com.zerobase.homemate.notification.push.dto.TokenWithIdDto(
                t.id, t.token
            )
            FROM FcmToken t
            JOIN UserNotificationSetting s ON s.user = t.user
            WHERE s.noticeEnabled= true
              AND t.isActive = true
              AND t.id > :lastId
            ORDER BY t.id
            LIMIT :limit
            """)
    List<TokenWithIdDto> findIdAndTokenBatch(@Param("lastId") long lastId, @Param("limit") int limit);
}
