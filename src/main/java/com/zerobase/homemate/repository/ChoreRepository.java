package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.Chore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

    List<Chore> findByCategoryChores_Category_Id(Long categoryId, Pageable pageable);
}
