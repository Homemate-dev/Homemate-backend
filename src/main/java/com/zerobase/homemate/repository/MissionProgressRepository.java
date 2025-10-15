package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.MissionProgress;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionProgressRepository extends JpaRepository<MissionProgress, Long> {

    List<MissionProgress> findByUserMission_IdInAndChoreInstance_Id(
        List<Long> userMissionIds,
        Long choreInstanceId);

    MissionProgress findByUserMission_IdAndChoreInstance_Id(
        Long userMissionId, Long choreInstanceId
    );
}
