package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import org.springframework.data.domain.Pageable;
import com.zerobase.homemate.entity.enums.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

    List<Chore> findByCategoryChores_Category_Id(Long categoryId, Pageable pageable);
    // Space 기준으로 사용자 chore 조회
    List<Chore> findBySpaceAndIsDeletedFalse(Space space);
}
