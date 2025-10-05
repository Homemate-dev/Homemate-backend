package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.SpaceChore;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {


    // SpaceChore 기준으로 사용자 chore 조회
    List<Chore> findBySpaceChoreAndIsDeletedFalse(SpaceChore spaceChore);


    Optional<Chore> findByTitle(String title);
}
