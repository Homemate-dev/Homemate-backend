package com.zerobase.homemate.repository;

import com.zerobase.homemate.entity.CategoryChore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CategoryChoreRepository extends JpaRepository<CategoryChore, Long> {

    List<CategoryChore> findByCategory_Id(Long categoryId, Pageable pageable);
}
