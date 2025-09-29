package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.SpaceChore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaceChoreRepository extends JpaRepository<SpaceChore, Long> {

    List<SpaceChore> findBySpaceId(Long spaceId);
}
