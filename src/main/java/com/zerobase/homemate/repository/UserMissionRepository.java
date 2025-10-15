package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.UserMission;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {

    List<UserMission> findByUser_IdAndMission_IdIn(Long userId, Collection<Long> missionIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    INSERT INTO user_mission (
        user_id, mission_id, current_count, is_completed, created_at, updated_at)
    VALUES (
        :userId, :missionId, 0, 0, NOW(), NOW()
    )
    ON DUPLICATE KEY UPDATE
        updated_at = VALUES(updated_at)
    """, nativeQuery = true)
    int upsert(@Param("userId") Long userId,
        @Param("missionId") Long missionId);
}
