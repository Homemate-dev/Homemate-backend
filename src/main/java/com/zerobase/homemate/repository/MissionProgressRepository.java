package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreInstance;
import com.zerobase.homemate.entity.MissionProgress;
import com.zerobase.homemate.entity.UserMission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionProgressRepository extends JpaRepository<MissionProgress, Long> {

    List<MissionProgress> findByUserMissionInAndChoreInstance(
        List<UserMission> userMissions,
        ChoreInstance choreInstance);

    MissionProgress findByUserMissionAndChoreInstance(
        UserMission userMission, ChoreInstance choreInstance
    );
}
