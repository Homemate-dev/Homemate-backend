package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

    List<Chore> findByCategoryId(Long categoryId, Pageable pageable);
}
