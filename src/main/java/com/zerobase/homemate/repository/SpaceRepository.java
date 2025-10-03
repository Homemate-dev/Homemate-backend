package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Space;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    // 단일 공간 조회 + chores eager fetch
    @EntityGraph(attributePaths = {"chores"})
    Optional<Space> findByIdAndIsActiveTrue(Long id);

    // 전체 활성화된 공간 조회
    List<Space> findAllByIsActiveTrue();

    Optional<Space> findByCode(String code);
}
