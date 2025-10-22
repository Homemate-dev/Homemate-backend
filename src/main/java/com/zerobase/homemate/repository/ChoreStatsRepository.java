package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.ChoreStats;
import com.zerobase.homemate.entity.enums.Category;
import com.zerobase.homemate.entity.enums.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChoreStatsRepository extends JpaRepository<ChoreStats, Integer> {

    Optional<ChoreStats> findByCategory(Category category);

    Optional<ChoreStats> findBySpace(Space space);
}
