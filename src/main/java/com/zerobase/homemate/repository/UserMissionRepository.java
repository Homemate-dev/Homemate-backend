package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.UserMission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {

    List<UserMission> findByUser_IdAndMission_IdIn(Long userId, Collection<Long> missionIds);
}
