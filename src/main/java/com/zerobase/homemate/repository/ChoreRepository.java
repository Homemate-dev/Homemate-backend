package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

    List<Chore> findByUserIdAndIsDeletedIsFalse(Long userId, Sort sort);

    Optional<Chore> findByUserIdAndTitle(Long id, String title);
}
