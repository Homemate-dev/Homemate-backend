package com.zerobase.homemate.repository;


import com.zerobase.homemate.entity.SpaceChore;
import com.zerobase.homemate.entity.enums.Space;
import com.zerobase.homemate.recommend.dto.SpaceChoreResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface SpaceChoreRepository extends JpaRepository<SpaceChore, Long> {
    // Space 기준으로 사용자 chore 조회
    @Query("""
    SELECT c
    from SpaceChore c
    WHERE c.space = :space
    ORDER BY function('RAND')
""")
    List<SpaceChore> findBySpace(Space space, Pageable pageable);

    @Query(value = "SELECT id, title_ko FROM space_chores ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<SpaceChoreResponse> findRandomChores();
           
    Optional<SpaceChore> findByTitleKo(String title);

    Long countBySpace(Space space);
}
