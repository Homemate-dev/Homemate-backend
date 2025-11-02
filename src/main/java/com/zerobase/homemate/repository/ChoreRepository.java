package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {


    boolean existsByUserIdAndTitle(Long userId, String title);

    Optional<Chore> findByUserIdAndTitle(Long id, String title);
}
