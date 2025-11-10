package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import com.zerobase.homemate.entity.enums.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {


    Optional<Chore> findByUserIdAndTitle(Long id, String title);

    Optional<Chore> findByUserIdAndTitleAndSpace(Long userId, String title, Space space);
}
