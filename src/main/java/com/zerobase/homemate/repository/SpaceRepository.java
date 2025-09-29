package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    Optional<Space> findBySpaceId(Long spaceId);
}
