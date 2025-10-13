package com.zerobase.homemate.repository;


import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SpaceChoreRepository extends JpaRepository<SpaceChore, Long> {

    Optional<SpaceChore> findBySpace(Space space);

}
